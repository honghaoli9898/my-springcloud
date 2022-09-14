package com.seaboxdata.sdps.extendAnalysis.utils.tenant;

import com.seaboxdata.sdps.common.framework.bean.analysis.tenant.DatabaseInfo;
import com.seaboxdata.sdps.common.framework.bean.analysis.tenant.ProjectInfo;
import com.seaboxdata.sdps.common.framework.bean.analysis.tenant.TableInfo;
import com.seaboxdata.sdps.common.framework.bean.analysis.tenant.TenantInfo;
import com.seaboxdata.sdps.common.framework.enums.DirFileType;
import com.seaboxdata.sdps.extendAnalysis.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TenantUtils {
    public static Map<String, TenantInfo> getTenantInfo(String sdpsItemCenterIpPort, String userIdKey, String userNameKey) {
        HashMap<String, TenantInfo> tenantMap = new HashMap();

        SdpsUserRelationProjectFetch relationFetch = new SdpsUserRelationProjectFetch();
        List<ProjectInfo> projectInfoList = relationFetch.fetchProjects(sdpsItemCenterIpPort, userIdKey, userNameKey);
        List<DatabaseInfo> databaseInfoList = relationFetch.fetchDbs(sdpsItemCenterIpPort, userIdKey, userNameKey);
        List<TableInfo> tableInfoList = relationFetch.fetchTables(sdpsItemCenterIpPort, userIdKey, userNameKey);
        setProjectTenant(projectInfoList, tenantMap);
        setDbTenant(databaseInfoList, tenantMap);
        setTableTenant(tableInfoList, tenantMap);
        return tenantMap;
    }

    public static void setProjectTenant(List<ProjectInfo> projects, Map<String, TenantInfo> tenantMap) {
        if (!CollectionUtils.isEmpty(projects)) {
            for (ProjectInfo project : projects) {
                TenantInfo tenantInfo = new TenantInfo();
                tenantInfo.setName(project.getIdentification());
                tenantInfo.setType(DirFileType.PROJECT);
                tenantInfo.setTypeValue(project.getIdentification());
                String key = tenantInfo.getType().getName().concat("#").concat(tenantInfo.getTypeValue());
                tenantMap.put(key, tenantInfo);
            }
        }
    }

    private static void setDbTenant(List<DatabaseInfo> dbs, Map<String, TenantInfo> tenantMap) {
        if (!CollectionUtils.isEmpty(dbs)) {
            for (DatabaseInfo db : dbs) {
                DirFileType type = null;
                if (db.getDbType().equalsIgnoreCase(RegexUtils.DBCategory.hive.name())) {
                    type = DirFileType.DATABASE_HIVE;
                }

                if (db.getDbType().equalsIgnoreCase(RegexUtils.DBCategory.hive_external.name())) {
                    type = DirFileType.DATABASE_EXTERNAL_HIVE;
                }

                if (db.getDbType().equalsIgnoreCase(RegexUtils.DBCategory.hbase.name())) {
                    type = DirFileType.DATABASE_HBASE;
                }

                if (type != null) {
                    TenantInfo tenantInfo = new TenantInfo();
                    tenantInfo.setName(db.getProjectIdent());
                    tenantInfo.setType(type);
                    tenantInfo.setTypeValue(db.getDbName());
                    String key = tenantInfo.getType().getName().concat("#").concat(tenantInfo.getTypeValue());
                    tenantMap.put(key, tenantInfo);
                }
            }
        }
    }

    private static void setTableTenant(List<TableInfo> tables, Map<String, TenantInfo> tenantMap) {
        if (!CollectionUtils.isEmpty(tables)) {
            for (TableInfo table : tables) {
                DirFileType type = null;
                if (table.getTableType().equalsIgnoreCase(RegexUtils.DBCategory.hive.name())) {
                    type = DirFileType.TABLE_HIVE;
                }

                if (table.getTableType().equalsIgnoreCase(RegexUtils.DBCategory.hive_external.name())) {
                    type = DirFileType.TABLE_EXTERNAL_HIVE;
                }

                if (table.getTableType().equalsIgnoreCase(RegexUtils.DBCategory.hbase.name())) {
                    type = DirFileType.TABLE_HBASE;
                }

                if (type != null) {
                    TenantInfo tenantInfo = new TenantInfo();
                    tenantInfo.setName(table.getProjectIdent());
                    tenantInfo.setType(type);
                    tenantInfo.setTypeValue(table.getDbName().concat(".").concat(table.getTableName()));
                    String key = tenantInfo.getType().getName().concat("#").concat(tenantInfo.getTypeValue());
                    tenantMap.put(key, tenantInfo);
                }
            }
        }
    }
}
