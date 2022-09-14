package com.seaboxdata.sdps.extendAnalysis.test;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;

public class TestJava {
    public static void main(String[] args) throws Exception {
//        JobCmdType[] cmdTypes = JobCmdType.values();
//        for (JobCmdType cmdType : cmdTypes) {
//            System.out.println(cmdType);
//        }
//        String[] cmdString = new String[]{"-cluster", "'tdw_core_tbds'", "-clusterType", "'TBDS'"};
//        Properties properties = BaseJobCmd.init(cmdString);
//        System.out.println(properties.toString());
//
//        String property = properties.getProperty("seabox.job.cluster.type");
//        System.out.println(property);

//        String[] cmdString = new String[]{"-cluster", "tdw_core_tbds", "-clusterType", "TBDS"};
//        CommandLine commandLine = BaseJobCmd.parseArgs(args);
//        String cluster = commandLine.getOptionValue(JobCmdType.CLUSTER_IP_HOST.getOpt());
//        System.out.println(cluster);


//        Properties properties = BaseJobCmd.init(args);
//        String clusterName = properties.getProperty(Constants.CLUSTER_NAME);
//        System.out.println(clusterName);

        //测试Tenant与项目、数据库、数据表的关系api
//        SdpsUserRelationProjectFetch fetch = new SdpsUserRelationProjectFetch();
//        List<ProjectInfo> projectInfoList = fetch.fetchProjects("10.1.0.42:9003", "1", "admin");
//        System.out.println(projectInfoList.toString());

//        List<DatabaseInfo> databaseInfos = fetch.fetchDbs("10.1.0.42:9003");
//        System.out.println(JSON.toJSONString(databaseInfos));

//        List<TableInfo> tableInfos = fetch.fetchTables("10.1.0.42:9003", "1", "admin");
//        System.out.println(tableInfos.toString());


//        String json = "{101:\"/testData\",102:\"/testData2\"}";
//        Map<Long,String> sdpsMergeDataIdPathMap = JSONObject.parseObject(json, Map.class);
//        for (Map.Entry<Long, String> entry : sdpsMergeDataIdPathMap.entrySet()) {
//            System.out.println(entry.getKey());
//            System.out.println(Long.valueOf(entry.getKey().toString()).getClass());
//            System.out.println(entry.getValue());
//        }

        FileUtils.write(new File("/data/a.txt"),"aaaaa","UTF-8");
    }
}
