package com.seaboxdata.sdps.extendAnalysis.utils.tenant;

import com.alibaba.fastjson.JSON;
import com.seaboxdata.sdps.common.framework.bean.analysis.tenant.DatabaseInfo;
import com.seaboxdata.sdps.common.framework.bean.analysis.tenant.ProjectInfo;
import com.seaboxdata.sdps.common.framework.bean.analysis.tenant.TableInfo;
import com.seaboxdata.sdps.common.framework.bean.dto.DatabaseDtoObj;
import com.seaboxdata.sdps.common.framework.bean.dto.ItemDtoObj;
import com.seaboxdata.sdps.common.framework.bean.dto.TableDtoObj;
import com.seaboxdata.sdps.extendAnalysis.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SdpsUserRelationProjectFetch {


    public List<ProjectInfo> fetchProjects(String sdpsItemCenterIpPort, String userIdKey, String userNameKey) {
        List<ProjectInfo> projectInfoList = new ArrayList<>();
        String resultJson = getSeaboxResultJson(sdpsItemCenterIpPort, Constants.SDPS_PROJECT_LIST_API, userIdKey, userNameKey);
        List<ItemDtoObj> itemDtoList = JSON.parseArray(resultJson, ItemDtoObj.class);
        for (ItemDtoObj itemDto : itemDtoList) {
            ProjectInfo projectInfo = new ProjectInfo();
            projectInfo.setProjectId(itemDto.getId());
            projectInfo.setProjectName(itemDto.getName());
            projectInfo.setIdentification(itemDto.getIden());
            projectInfoList.add(projectInfo);
        }
        return projectInfoList;
    }

    public List<DatabaseInfo> fetchDbs(String sdpsItemCenterIpPort, String userIdKey, String userNameKey) {
        List<DatabaseInfo> databaseInfoList = new ArrayList<>();
        String resultJson = getSeaboxResultJson(sdpsItemCenterIpPort, Constants.SDPS_DB_LIST_API, userIdKey, userNameKey);
        List<DatabaseDtoObj> databaseDtoObjs = JSON.parseArray(resultJson, DatabaseDtoObj.class);
        for (DatabaseDtoObj databaseDtoObj : databaseDtoObjs) {
            DatabaseInfo databaseInfo = new DatabaseInfo();
            databaseInfo.setId(databaseDtoObj.getId());
            databaseInfo.setDbType(databaseDtoObj.getType());
            databaseInfo.setDbName(databaseDtoObj.getName());
            if (StringUtils.isNotBlank(databaseDtoObj.getDesc())) {
                databaseInfo.setDes(databaseDtoObj.getDesc());
            }
            databaseInfo.setProjectId(databaseDtoObj.getAssItemId());
            databaseInfo.setProjectIdent(databaseDtoObj.getAssItemIden());
            databaseInfo.setProjectName(databaseDtoObj.getAssItemName());
            databaseInfo.setCreatorName(databaseDtoObj.getCreateUser());
            databaseInfo.setClusterId(databaseDtoObj.getClusterId());

            databaseInfoList.add(databaseInfo);
        }

        return databaseInfoList;
    }

    public List<TableInfo> fetchTables(String sdpsItemCenterIpPort, String userIdKey, String userNameKey) {
        List<TableInfo> tableInfoList = new ArrayList<>();
        String resultJson = getSeaboxResultJson(sdpsItemCenterIpPort, Constants.SDPS_TABLE_LIST_API, userIdKey, userNameKey);
        List<TableDtoObj> tableDtoObjList = JSON.parseArray(resultJson, TableDtoObj.class);
        for (TableDtoObj tableDtoObj : tableDtoObjList) {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setId(tableDtoObj.getId());
            tableInfo.setTableName(tableDtoObj.getEnName());
            if (StringUtils.isNotBlank(tableDtoObj.getDescription())) {
                tableInfo.setDes(tableDtoObj.getDescription());
            }
            tableInfo.setTableType(tableDtoObj.getType());
            tableInfo.setDbName(tableDtoObj.getDatabaseName());
            tableInfo.setProjectId(tableDtoObj.getItemId());
            tableInfo.setProjectIdent(tableDtoObj.getAssItemIden());
            tableInfo.setClusterId(tableDtoObj.getClusterId());

            tableInfoList.add(tableInfo);
        }

        return tableInfoList;
    }


    public String getSeaboxResultJson(String ipAndPort, String restApiUrl, String userIdKey, String userNameKey) {
        String resultJson = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            String url = "http://".concat(ipAndPort).concat(restApiUrl);
            URIBuilder uriBuilder = new URIBuilder(url);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.addHeader(Constants.HTTP_CONTENT_TYPE, Constants.HTTP_CONTENT_TYPE_JSON);
            httpGet.addHeader(Constants.HTTP_HEADER_SDPS_USER_ID_KEY, userIdKey);
            httpGet.addHeader(Constants.HTTP_HEADER_SDPS_USER_NAME_KEY, userNameKey);
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                resultJson = EntityUtils.toString(response.getEntity(), "utf8");
            }
        } catch (Exception e) {
            log.error("获取SDPS项目用户关系异常", e);
            System.exit(1);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error("response 关闭异常:", e);
                System.exit(1);
            }
        }
        return resultJson;
    }
}
