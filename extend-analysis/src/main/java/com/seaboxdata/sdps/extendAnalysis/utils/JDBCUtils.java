package com.seaboxdata.sdps.extendAnalysis.utils;

import com.seaboxdata.sdps.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.SparkFiles;

import javax.sql.DataSource;
import java.io.File;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

@Slf4j
public class JDBCUtils {
    private String url;

    public JDBCUtils(String url) {
        this.url = url;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        } catch (Exception e) {
            log.error("init jdbc driver error:", e);
            System.exit(1);
        }
    }

    public void executeBatchKerberos(String sql, List<List<Object>> argsList,String zkQuorum,String znode,
                                     String krb5File,String hbasePrincipalName,String hbaseUserkeytabFile,
                                     String hbaseServerPrincipal,String hbaseServerKeytabFile,
                                     String phoenixQueryserverPrincipal,String phoenixQueryserverKeytabFile,
                                     Configuration hbaseConf
                                     ){
        System.setProperty("java.security.krb5.conf",krb5File);
        UserGroupInformation.setConfiguration(hbaseConf);
        UserGroupInformation userGroupInformation = null;
        try {
            File file = new File(hbaseUserkeytabFile);
            log.info("keytab文件路径");
            log.info(file.getPath());
            log.info("路径是否存在:"+String.valueOf(file.exists()));
            log.info("hbasePrincipalName:"+hbasePrincipalName);
            log.info("hbaseUserkeytabFile:"+hbaseUserkeytabFile);
            userGroupInformation = UserGroupInformation.loginUserFromKeytabAndReturnUGI(hbasePrincipalName,hbaseUserkeytabFile);
        } catch (Exception e) {
            log.error("身份认证异常:{} ", e);
            throw new BusinessException("身份认证异常:".concat(e.getMessage()));
        }
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", zkQuorum);
        properties.setProperty("hbase.master.kerberos.principal",hbaseServerPrincipal);
        properties.setProperty("hbase.master.keytab.file", hbaseServerKeytabFile);
        properties.setProperty("hbase.regionserver.kerberos.principal",hbaseServerPrincipal);
        properties.setProperty("hbase.regionserver.keytab.file", hbaseServerKeytabFile);
        properties.setProperty("phoenix.queryserver.kerberos.principal", phoenixQueryserverPrincipal);
        properties.setProperty("phoenix.queryserver.keytab.file", phoenixQueryserverKeytabFile);
        properties.setProperty("hbase.security.authentication", "kerberos");
        properties.setProperty("hadoop.security.authentication", "kerberos");
        properties.setProperty("zookeeper.znode.parent", znode);
        userGroupInformation.doAs(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    executeBatch(sql,argsList,properties);
                } catch (Exception e) {
                    log.error("执行批量查询异常:", e);
                }
                return null;
            }
        });
    }

    public void executeBatch(String sql, List<List<Object>> argsList,Properties props) {
        if (!CollectionUtils.isEmpty(argsList)) {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                props.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");
                props.setProperty("phoenix.schema.mapSystemTablesToNamespace", "true");
                conn = DriverManager.getConnection(this.url, props);
                stmt = conn.prepareStatement(sql);

                conn.setAutoCommit(false);
                Iterator<List<Object>> iterator = argsList.iterator();
                while (iterator.hasNext()) {
                    List<Object> args = (List) iterator.next();
                    this.setSqlParams(stmt, args);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();

            } catch (Exception e) {
                log.error("JDBCUtils executeBatch error:", e);
                System.exit(1);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception e) {
                    log.error("jdbc executeBatch close error:", e);
                    System.exit(1);
                }
            }
        }
    }

    private void setSqlParams(PreparedStatement stmt, List<Object> args) throws SQLException {
        for (int i = 0; i < args.size(); ++i) {
            Object object = args.get(i);
            int sqlIndex = i + 1;
            if (object instanceof String) {
                stmt.setString(sqlIndex, (String) object);
            }

            if (object instanceof Long) {
                stmt.setLong(sqlIndex, (Long) object);
            }

            if (object instanceof Double) {
                stmt.setDouble(sqlIndex, (Double) object);
            }

            if (object instanceof Integer) {
                stmt.setInt(sqlIndex, (Integer) object);
            }
        }
    }

    public void execute(String sql) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(this.url);
            stmt = conn.prepareStatement(sql);
            stmt.execute();
        } catch (Exception e) {
            log.error("JDBCUtils execute error:", e);
            System.exit(1);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error("jdbc execute close error:", e);
                System.exit(1);
            }
        }
    }

    public void execute(String sql, List<Object> args) {
        if (!CollectionUtils.isEmpty(args)) {
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = DriverManager.getConnection(this.url);
                stmt = conn.prepareStatement(sql);
                this.setSqlParams(stmt, args);
                stmt.execute();
            } catch (Exception e) {
                log.error("JDBCUtils execute error:", e);
                System.exit(1);
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception e) {
                    log.error("jdbc execute close error:", e);
                    System.exit(1);
                }
            }
        }
    }
}
