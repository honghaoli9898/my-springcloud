package com.seaboxdata.sdps.extendAnalysis.common.cmd;

import com.seaboxdata.sdps.extendAnalysis.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import java.util.Properties;

@Slf4j
public class BaseJobCmd {

    public static Properties init(String[] args) {

        Properties properties = new Properties();
        //解析
        CommandLine cmd = parseArgs(args);

        String opt = JobCmdType.CLUSTER.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.CLUSTER_NAME, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.CLUSTER_ID.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.CLUSTER_ID, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.CLUSTER_TYPE.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.CLUSTER_TYPE, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.CLUSTER_IP_HOST.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.CLUSTER_IP_HOST, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.JDBC_URL.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.JDBC_URL, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_FSIMAGE_DAY_TIME.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_FSIMAGE_DAY_TIME, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_FSIMAGE_FILE_PATH.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_FSIMAGE_FILE_PATH, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_FSIMAGE_FILE_TYPE.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_FSIMAGE_FILE_TYPE, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_FSIMAGE_FILE_SEPARATOR.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_FSIMAGE_FILE_SEPARATOR, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_FSIMAGE_FILE_HEADER.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_FSIMAGE_FILE_HEADER, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_FSIMAGE_HBASE_QUORUM.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_FSIMAGE_HBASE_QUORUM, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_FSIMAGE_HBASE_PORT.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_FSIMAGE_HBASE_PORT, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_FSIMAGE_HBASE_ZNODE.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_FSIMAGE_HBASE_ZNODE, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.CLUSTER_CONF_API.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.CLUSTER_CONF_API, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.DOWNLOAD_KRB5_API.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.DOWNLOAD_KRB5_API, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.PROJECT_RELATION_URL.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.PROJECT_RELATION_URL, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.PROJECT_USER_ID.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HTTP_HEADER_SDPS_USER_ID_KEY, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.PROJECT_USER_NAME.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HTTP_HEADER_SDPS_USER_NAME_KEY, cmd.getOptionValue(opt));
        }

        /**
         * 合并小文件参数
         */
        opt = JobCmdType.HDFS_MERGE_DIR_PATH.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_MERGE_DIR_PATH, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_MERGE_FILE_CODEC.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_MERGE_FILE_CODEC, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_MERGE_REAL_USER.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_MERGE_REAL_USER, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.HDFS_MERGE_REAL_GROUP.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.HDFS_MERGE_REAL_GROUP, cmd.getOptionValue(opt));
        }

        opt = JobCmdType.SDPS_JOB_ID.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.SDPS_JOB_ID, cmd.getOptionValue(opt));
        }

//        opt = JobCmdType.SDPS_MERGE_SUBMIT_ID.getOpt();
//        if (cmd.hasOption(opt)) {
//            properties.put(Constants.SDPS_MERGE_SUBMIT_ID, cmd.getOptionValue(opt));
//        }

        opt = JobCmdType.SDPS_MERGE_DATA_ID_PATH_JSON.getOpt();
        if (cmd.hasOption(opt)) {
            properties.put(Constants.SDPS_MERGE_DATA_ID_PATH_JSON, cmd.getOptionValue(opt));
        }
        return properties;
    }


    public static CommandLine parseArgs(String[] args) {

        CommandLine cmd = null;
        try {
            Options options = new Options();
            JobCmdType[] cmdTypes = JobCmdType.values();

            for (JobCmdType cmdType : cmdTypes) {
                Option option = new Option(cmdType.getOpt(), cmdType.getLongOpt(), cmdType.getHasArg(), cmdType.getDes());
                option.setRequired(cmdType.getRequired());
                options.addOption(option);
            }
            //CLI 解析阶段
            cmd = (new PosixParser()).parse(options, args);

        } catch (Exception e) {
            log.error("解析参数异常:", e);
            System.exit(1);
        }
        return cmd;
    }

}
