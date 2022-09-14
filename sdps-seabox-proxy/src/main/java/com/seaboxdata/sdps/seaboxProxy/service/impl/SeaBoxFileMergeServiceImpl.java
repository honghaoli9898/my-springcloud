package com.seaboxdata.sdps.seaboxProxy.service.impl;

import com.clearspring.analytics.util.Lists;
import com.google.common.base.Preconditions;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.SdpsContentSummary;
import com.seaboxdata.sdps.common.framework.bean.SdpsFileStatus;
import com.seaboxdata.sdps.common.framework.bean.dto.FileMergeDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.HiveTableDTO;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeDetailRequest;
import com.seaboxdata.sdps.common.framework.bean.request.FileMergeRequest;
import com.seaboxdata.sdps.seaboxProxy.bean.SdpsDirPathInfo;
import com.seaboxdata.sdps.seaboxProxy.constants.DataSourceType;
import com.seaboxdata.sdps.seaboxProxy.service.ISeaBoxFileMergeService;
import com.seaboxdata.sdps.seaboxProxy.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeaBoxFileMergeServiceImpl implements ISeaBoxFileMergeService {
    @Autowired
    BigdataVirtualHost bigdataVirtualHost;
    @Override
    public HiveTableDTO checkTableMerge(FileMergeRequest fileMergeRequest) {
        //查询hive表信息
        //设置虚拟DNS
        bigdataVirtualHost.setVirtualHost(fileMergeRequest.getClusterId());
        HiveUtil hiveUtil = new HiveUtil(fileMergeRequest.getClusterId());
        Table table = hiveUtil.getTable(fileMergeRequest.getDbName(), fileMergeRequest.getTable());
        HiveTableDTO hiveTableDTO = getByHiveTable(fileMergeRequest.getClusterId(), table, fileMergeRequest.getType());
        return hiveTableDTO;
    }

    @Override
    public FileMergeDTO getFileMergeDetail(FileMergeDetailRequest fileMergeDetailRequest) {
        log.info("param:{}", fileMergeDetailRequest);
        //检查参数
        checkParam(fileMergeDetailRequest);

        FileMergeDTO fileMergeDTO = null;
        String type = fileMergeDetailRequest.getType();
        if (DataSourceType.HIVE_TYPE.equals(type) || DataSourceType.HIVE_EXTERNAL_TYPE.equals(type)) {
            fileMergeDTO = getTableMergeDetail(fileMergeDetailRequest);
        } else if (DataSourceType.HDFS_TYPE.equals(type)) {
            fileMergeDTO = getHdfsMergeDetail(fileMergeDetailRequest);
        }
        return fileMergeDTO;
    }

    private FileMergeDTO getHdfsMergeDetail(FileMergeDetailRequest fileMergeDetailRequest) {
        FileMergeDTO fileMergeDTO = new FileMergeDTO();
        String path = fileMergeDetailRequest.getPath();
        fileMergeDTO.setPath(path);
        fileMergeDTO.setBasePath(path);

        HdfsUtil hdfsUtil = new HdfsUtil(fileMergeDetailRequest.getClusterId());
        SdpsContentSummary contentSummary = hdfsUtil.getContentSummary(path);
        fileMergeDTO.setContentSummary(contentSummary);

        List<SdpsFileStatus> childrenFileStatus = hdfsUtil.getChildrenFileStatus(path);
        fileMergeDTO.setFileStatusList(childrenFileStatus);

        SdpsDirPathInfo dirInfo = getDirPathInfo(fileMergeDetailRequest.getClusterId(), path);
        if (dirInfo != null && dirInfo.getFormatType() != null) {
            fileMergeDTO.setFormat(dirInfo.getFormatType().getName());
        }

        if (dirInfo != null && dirInfo.getCodecType() != null) {
            fileMergeDTO.setCodec(dirInfo.getCodecType().getName());
        }
        return fileMergeDTO;
    }

    private void checkParam(FileMergeDetailRequest fileMergeDetailRequest) {
        Preconditions.checkNotNull(fileMergeDetailRequest);
        if (StringUtils.isBlank(fileMergeDetailRequest.getType())) {
            throw new BusinessException("类型为空");
        }
        if (DataSourceType.HDFS_TYPE.equals(fileMergeDetailRequest.getType())) {
            Preconditions.checkNotNull(fileMergeDetailRequest.getPath());
            if (StringUtils.isBlank(fileMergeDetailRequest.getPath())) {
                throw new BusinessException("路径为空");
            }
        } else {
            if (StringUtils.isBlank(fileMergeDetailRequest.getStartTime())) {
                throw new BusinessException("开始时间为空");
            }
            if (StringUtils.isBlank(fileMergeDetailRequest.getEndTime())) {
                throw new BusinessException("结束时间为空");
            }
            if (StringUtils.isBlank(fileMergeDetailRequest.getDbName())) {
                throw new BusinessException("库名为空");
            }
            if (StringUtils.isBlank(fileMergeDetailRequest.getTable())) {
                throw new BusinessException("表名为空");
            }
        }
    }

    private FileMergeDTO getTableMergeDetail(FileMergeDetailRequest fileMergeDetailRequest) {
        FileMergeDTO fileMergeDTO = new FileMergeDTO();
        String path = fileMergeDetailRequest.getPath();
        fileMergeDTO.setPath(path);

        HiveTableDTO schema = checkTableMerge(fileMergeDetailRequest);
        String basePath = (new Path(schema.getPathLocation())).toUri().getPath();
        fileMergeDTO.setBasePath(basePath);

        HdfsUtil hdfsUtil = new HdfsUtil(fileMergeDetailRequest.getClusterId());
        List<String> pathList = getMergeTablePaths(fileMergeDetailRequest, schema, hdfsUtil);

        SdpsContentSummary contentSummary = hdfsUtil.getContentSummaryByPaths(pathList);
        fileMergeDTO.setContentSummary(contentSummary);

        List<SdpsFileStatus> statusList = pathList.stream().map(dir -> hdfsUtil.getDirSummary(dir)).collect(Collectors.toList());
        fileMergeDTO.setFileStatusList(statusList);

        String sourcePath = StringUtils.join(pathList, ",");
        fileMergeDTO.setPath(sourcePath);

        String codecByPath = getTableCodecByPath(fileMergeDetailRequest.getClusterId(), pathList);
        String codec = StringUtils.isNotBlank(codecByPath) ? codecByPath : schema.getCodec();
        fileMergeDTO.setCodec(codec);

        fileMergeDTO.setFormat(schema.getFormat());

        fileMergeDTO.setTableInfo(fileMergeDetailRequest);
        return fileMergeDTO;
    }

    /**
     * 根据路径获取表的压缩编码
     * @param clusterId 集群id
     * @param pathList  路径集合
     * @return
     */
    private String getTableCodecByPath(Integer clusterId, List<String> pathList) {
        String codec = null;
        if (!CollectionUtils.isEmpty(pathList)) {
            SdpsDirPathInfo dirPathInfo = getDirPathInfo(clusterId, pathList.get(0));
            if (dirPathInfo != null && dirPathInfo.getCodecType() != null) {
                codec = dirPathInfo.getCodecType().getCodec();
            }
        }
        return codec;
    }

    /**
     * 获取表分区路径集合
     * @param fileMergeDetailRequest
     * @param schema
     * @param hdfsUtil
     * @return
     */
    private List<String> getMergeTablePaths(FileMergeDetailRequest fileMergeDetailRequest, HiveTableDTO schema, HdfsUtil hdfsUtil) {
        List<String> pathList = Lists.newArrayList();
        if (schema.getDatePartition()) {
            try {
                String prefix = schema.getParKeyPrefix(fileMergeDetailRequest.getType());
                SimpleDateFormat sdf = SeaboxDateUtil.getFormatByPattern(schema.getDatePattern());
                Long startTime = sdf.parse(fileMergeDetailRequest.getStartTime()).getTime();
                Long endTime = sdf.parse(fileMergeDetailRequest.getEndTime()).getTime();

                SimpleDateFormat sdf1 = new SimpleDateFormat(schema.getDateFormat());
                List<SdpsFileStatus> fileStatusList = hdfsUtil.getChildrenFileStatus(schema.getPathLocation());
                for (SdpsFileStatus fileStatus : fileStatusList) {
                    String dateKey = StringUtils.substringAfter(fileStatus.getFileName(), prefix);
                    Long parseTime = sdf1.parse(dateKey).getTime();
                    //如果分区的时间在查询范围内，则add到pathList中
                    if (parseTime != null && parseTime >= startTime && parseTime <= endTime) {
                        pathList.add(fileStatus.getPath());
                    }
                }
            } catch (Exception e) {
                log.error("获取表的hdfs路径报错", e);
            }

        }

        return pathList;
    }

    /**
     * 设置表信息
     * @param clusterId 集群id
     * @param table     hive表信息
     * @param category  表类型：hive内表、hive外表
     * @return
     */
    private HiveTableDTO getByHiveTable(Integer clusterId, Table table, String category) {
        HiveTableDTO tableDTO = new HiveTableDTO();
        BeanUtils.copyProperties(table, tableDTO);
        StorageDescriptor sd = table.getSd();
        tableDTO.setPathLocation(HdfsUtil.getHdfsPathLocation(sd.getLocation()));
        List<String> partKeys = table.getPartitionKeys() == null ? Collections.emptyList() : table.getPartitionKeys().stream().map(FieldSchema::getName).collect(Collectors.toList());
        tableDTO.setPartKeys(partKeys);
        String firstPart = partKeys.size() > 0 ? partKeys.get(0) : null;
        tableDTO.setPartKey(firstPart);
        tableDTO.setInputFormat(sd.getInputFormat());
        tableDTO.setOutputFormat(sd.getOutputFormat());
        tableDTO.setCodec("UNCOMPRESSED");
        //设置路径
        String partitionPrefix = tableDTO.getParKeyPrefix(category);
        if (!StringUtils.isBlank(partitionPrefix)) {
            HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
            ArrayList<HdfsFSObj> hdfsFSObjs = hdfsUtil.selectHdfsSaveObjList(tableDTO.getPathLocation());
            //如果表中数据不为空，则获取目录名判断是否是分区目录
            if (!CollectionUtils.isEmpty(hdfsFSObjs)) {
                SimpleDateFormat sdf = null;

                String pathSuffix;
                for (HdfsFSObj hdfsFSObj : hdfsFSObjs) {
                    pathSuffix = hdfsFSObj.getFileName();
                    if (sdf == null) {
                        String date = StringUtils.substringAfter(pathSuffix, partitionPrefix);
                        sdf = SeaboxDateUtil.parseFormat(date);
                    }
                    tableDTO.getTablePaths().add(pathSuffix);
                }

                if (sdf != null) {
                    tableDTO.setDatePartition(true);
                    tableDTO.setDateFormat(sdf.toPattern());
                    tableDTO.setDatePattern(SeaboxDateUtil.parsePattern(sdf));
                }

            }
        }
        return tableDTO;
    }



    /**
     * 获取路径状态信息
     * @param clusterId 集群id
     * @param path      hdfs路径
     * @return
     */
    private SdpsDirPathInfo getDirPathInfo(Integer clusterId, String path) {
        SdpsDirPathInfo dirInfo = null;

        try {
            HdfsUtil hdfsUtil = new HdfsUtil(clusterId);
            dirInfo = hdfsUtil.getDirPathInfo(path, true);
        } catch (Exception e) {
            log.error("can't get dir path info of souce path=" + path, e);
        }

        return dirInfo;
    }
}
