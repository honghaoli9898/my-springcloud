package com.seaboxdata.sdps.extendAnalysis.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.extendAnalysis.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.extendAnalysis.mapper.SysGlobalArgsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.apache.hadoop.hdfs.tools.DFSAdmin;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.security.PrivilegedAction;

@Slf4j
public class HdfsUtil {
    private FileSystem fs;
    private String userName = "hdfs";
    private Boolean isEnableKerberos = false;
    private UserGroupInformation userGroupInformation;

    /**
     * 构造
     *
     * @param clusterId
     */
    public HdfsUtil(Integer clusterId, Configuration hdfsConf,
                    String krb5FileName,String hdfsPrincipalName,String keytabFile,
                    Boolean isClusterEnablekerberos,Boolean isDBConfEnablekerberos) {
        try {
            init(clusterId,hdfsConf,krb5FileName,hdfsPrincipalName,keytabFile,isClusterEnablekerberos,isDBConfEnablekerberos);
        } catch (Exception e) {
            log.error("HdfsUtil初始化异常:", e);
        }
    }

    /**
     * 初始化HdfsUtil
     *
     * @param clusterId
     * @throws Exception
     */
    private void init(Integer clusterId, Configuration hdfsConf,String krb5FileName,String hdfsPrincipalName,String keytabFile,
                      Boolean isClusterEnablekerberos,Boolean isDBConfEnablekerberos) throws Exception {

        hdfsConf.setBoolean(CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,true);
        if (isClusterEnablekerberos && isDBConfEnablekerberos) {
            isEnableKerberos = true;
            System.setProperty("java.security.krb5.conf",krb5FileName);
            System.setProperty("javax.security.auth.useSubjectCredsOnly","false");
            hdfsConf.set("hadoop.security.authentication", "kerberos");
            UserGroupInformation.setConfiguration(hdfsConf);
            try {
                String kerberosUsername = null;
                UserGroupInformation userGroupInformation = UserGroupInformation
                        .loginUserFromKeytabAndReturnUGI(hdfsPrincipalName,keytabFile);
                this.userGroupInformation = userGroupInformation;
            }catch (Exception e) {
                log.error("身份认证异常:{} ", e);
                throw new BusinessException("身份认证异常:".concat(e.getMessage()));
            }
            this.userGroupInformation.doAs(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        fs = FileSystem.get(FileSystem.getDefaultUri(hdfsConf), hdfsConf);
                    } catch (Exception e) {
                        log.error("获取fileSystem报错:{}", e);
                    }
                    return null;
                }
            });
        }else {
            fs = FileSystem.get(FileSystem.getDefaultUri(hdfsConf), hdfsConf,userName);
        }

    }

    /**
     * 获取FS对象
     *
     * @return
     */
    public FileSystem getFs() {
        return fs;
    }
}
