package com.seaboxdata.sdps.seaboxProxy.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.utils.excelutil.DateUtil;
import com.seaboxdata.sdps.seaboxProxy.constants.BigDataConfConstants;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.util.AmbariUtil;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;
import com.seaboxdata.sdps.seaboxProxy.util.HdfsUtil;


@Slf4j
@Service
public class MetaDataExtract {

    @Autowired
    BigdataVirtualHost bigdataVirtualHost;

    @Autowired
    BigdataCommonFegin bigdataCommonFegin;

    /**
     * hdfs元数据fsimage、解析后的文件临时目录
     */
    @Value("${metaData.hdfs.fsimageAndExtractLocalTempPath}")
    String metaDataHdfsFsimageAndExtractLocalTempPath;

    /**
     * hdfs元数据fsimage、解析后的文件HDFS保存目录
     */
    @Value("${metaData.hdfs.fsimageAndExtractHdfsPath}")
    String metaDataHdfsFsimageAndExtractHdfsPath;

    /**
     * 临时存储目录
     */
    @Value("${metaData.hdfs.tempPath}")
    String metaDataHdfsTempPath;

    public boolean fetchAndExtractHdfsMetaData(Integer clusterId) {
        Boolean flag = false;
        HdfsUtil hdfsUtil = null;
        try {
//            SdpsCluster sdpsCluster = bigdataCommonFegin.querySdpsClusterById(clusterId);
            SdpsCluster sdpsCluster = new SdpsCluster();
            sdpsCluster.setClusterId(clusterId);
            String clusterName = new AmbariUtil(clusterId).getClusterName();
            sdpsCluster.setClusterName(clusterName);

            bigdataVirtualHost.setVirtualHost(sdpsCluster.getClusterId());
            hdfsUtil = new HdfsUtil(sdpsCluster.getClusterId());

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYY_MM_DD);
            //前一天时间
            String formatTime = simpleDateFormat.format(DateUtils.addDays(new Date(), -1));
            String savePath = metaDataHdfsFsimageAndExtractLocalTempPath + File.separator +
                    sdpsCluster.getClusterName() + "#" + sdpsCluster.getClusterId();
            String saveHdfsPath = metaDataHdfsFsimageAndExtractHdfsPath + File.separator +
                    sdpsCluster.getClusterName() + "#" + sdpsCluster.getClusterId();
            String tempPath = metaDataHdfsTempPath + File.separator +
                    sdpsCluster.getClusterName() + "#" + sdpsCluster.getClusterId();
            log.info("拉取HDFS元数据并提取HDFS元数据,日期:{},本地保存临时目录:{},HDFS存储目录:{},日志临时目录:{}",formatTime,savePath,saveHdfsPath,tempPath);
            //fsimage名称(fsimage_年月日时分秒)
            String fsimageName = BigDataConfConstants.HDFS_META_DATA_IMAGE_FILE_PREFIX + "_" + formatTime;
            //extract名称(extract_年月日时分秒)
            String extractName = BigDataConfConstants.HDFS_META_DATA_EXTRACT_FILE_PREFIX + "_" + formatTime;

            //保存目录不存在则创建
            if (!new File(savePath).exists()) {
                new File(savePath).mkdirs();
            }
            //临时目录不存在则创建
            if (!new File(tempPath).exists()) {
                new File(tempPath).mkdirs();
            }

            String fsimageFile = savePath + File.separator + fsimageName;
            String extractFile = savePath + File.separator + extractName;
            //拉取HDFS元数据
            boolean fsimageBool = hdfsUtil.fetchHdfsImage(new File(fsimageFile));
            if (fsimageBool) {
                Boolean extractBool = hdfsUtil.extractHdfsMetaData(fsimageFile, extractFile, BigDataConfConstants.EXTRACT_HDFS_SOURCE_DATA_DELIMITER, tempPath);
                if (extractBool) {
                    FileSystem fs = hdfsUtil.getFs();
                    //HDFS元数据存储目录不存在则创建
                    boolean existsHdfsPath = fs.exists(new Path(saveHdfsPath));
                    if (!existsHdfsPath) {
                        fs.mkdirs(new Path(saveHdfsPath));
                    }
                    fs.copyFromLocalFile(false, new Path(fsimageFile), new Path(saveHdfsPath + File.separator + fsimageName));
                    fs.copyFromLocalFile(false, new Path(extractFile), new Path(saveHdfsPath + File.separator + extractName));
                    flag = true;
                }
            }


        } catch (Exception e) {
            log.error("拉取HDFS元数据并提取HDFS元数据异常:", e);
            flag = false;
        }finally {
            hdfsUtil.closeFs();
        }
        return flag;
    }
}
