package com.seaboxdata.sdps.seaboxProxy.service.impl;

import com.clearspring.analytics.util.Lists;
import com.google.common.base.Preconditions;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.framework.bean.SdpsDirExpire;
import com.seaboxdata.sdps.common.framework.bean.dto.StorageDTO;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.service.ISeaBoxStorageManagerService;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;
import com.seaboxdata.sdps.seaboxProxy.util.HdfsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SeaBoxStorageManagerServiceImpl implements ISeaBoxStorageManagerService {
    @Autowired
    private BigdataCommonFegin bigdataCommonFegin;
    @Autowired
    BigdataVirtualHost bigdataVirtualHost;

    @Override
    public void batchAddHdfsDir(StorageManageRequest request) {
        //检查参数
        checkAddParam(request);
        //设置虚拟DNS
        bigdataVirtualHost.setVirtualHost(request.getClusterId());

        HdfsUtil hdfsUtil = new HdfsUtil(request.getClusterId());
        //判断路径是否存在
        request.getPaths().forEach(path -> {
            if (hdfsUtil.isExist(path)) {
                throw new BusinessException(path + "该路径已存在");
            }
        });
        addAndSetQuota(request, hdfsUtil);
    }

    /**
     * 新增目录，设置配额
     * @param request
     * @param hdfsUtil
     */
    private void addAndSetQuota(StorageManageRequest request, HdfsUtil hdfsUtil) {
        request.getPaths().forEach(path -> {
            hdfsUtil.mkdir(path);
            setQuota(request, hdfsUtil, path);
        });
    }

    @Override
    public void batchSetQuota(StorageManageRequest request) {
        //检查参数
        checkAddParam(request);
        //设置虚拟DNS
        bigdataVirtualHost.setVirtualHost(request.getClusterId());

        HdfsUtil hdfsUtil = new HdfsUtil(request.getClusterId());
        //判断路径是否存在
        request.getPaths().forEach(path -> {
            if (!hdfsUtil.isExist(path)) {
                throw new BusinessException(path + "该路径不存在");
            }
        });

        //设置配额
        request.getPaths().forEach(path -> {
            setQuota(request, hdfsUtil, path);
        });
    }

    @Override
    public List<StorageDTO> getStorageList(StorageManageRequest request) {
        List<StorageDTO> storageList = Lists.newArrayList();
        //设置虚拟DNS
        bigdataVirtualHost.setVirtualHost(request.getClusterId());
        HdfsUtil hdfsUtil = new HdfsUtil(request.getClusterId());
        FileSystem fs = hdfsUtil.getFs();
        try {
            FileStatus[] fileStatuses = fs.listStatus(new Path(request.getParentPath()));
            for (FileStatus file : fileStatuses) {
                StorageDTO storageDTO = new StorageDTO();
                storageDTO.setPath(request.getParentPath() + file.getPath().getName());
                storageDTO.setOwner(file.getOwner());
                storageDTO.setGroup(file.getGroup());
                storageDTO.setStorageSize(file.getLen());
                storageDTO.setPermission(file.getPermission().toString());
                ContentSummary contentSummary = fs.getContentSummary(file.getPath());
                storageDTO.setNsQuota(contentSummary.getQuota());
                storageDTO.setDsQuota(contentSummary.getSpaceQuota());
                //设置过期时间
                SdpsDirExpire sdpsDirExpire = bigdataCommonFegin.selectByPath(request.getClusterId(), file.getPath().toString());
                if (sdpsDirExpire != null && sdpsDirExpire.getExpireDay() != null) {
                    storageDTO.setExpire(sdpsDirExpire.getExpireDay());
                }

                storageList.add(storageDTO);
            }
        } catch (Exception e) {
            log.error("查询hdfs目录信息报错", e);
            throw new BusinessException("查询hdfs目录信息报错");
        }

        return storageList;
    }

    /**
     * 设置配额
     * @param request
     * @param hdfsUtil
     */
    private void setQuota(StorageManageRequest request, HdfsUtil hdfsUtil, String path) {
        if (request.getEnableNsQuota()) {
            hdfsUtil.setNsQuota(path, request.getNsQuota());
        } else if (!request.getIsInsert()) {
            hdfsUtil.clearNsQuota(path);
        }
        if (request.getEnableDsQuota()) {
            hdfsUtil.setDsQuota(path, request.getDsQuota());
        } else if (!request.getIsInsert()) {
            hdfsUtil.clearDsQuota(path);
        }
    }

    /**
     * 检查参数
     * @param request
     */
    private void checkAddParam(StorageManageRequest request) {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(CollectionUtils.isEmpty(request.getPaths()));
        if (request.getEnableNsQuota()) {
            Preconditions.checkNotNull(request.getNsQuota());
        }
        if (request.getEnableDsQuota()) {
            Preconditions.checkNotNull(request.getDsQuota());
        }
    }
}
