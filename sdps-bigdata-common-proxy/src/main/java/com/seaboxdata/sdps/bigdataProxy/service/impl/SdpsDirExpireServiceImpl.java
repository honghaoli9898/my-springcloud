package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.base.Preconditions;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsDirExpireMapper;
import com.seaboxdata.sdps.bigdataProxy.service.ISdpsDirExpireService;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.framework.bean.SdpsDirExpire;
import com.seaboxdata.sdps.common.framework.bean.request.StorageManageRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SdpsDirExpireServiceImpl extends SuperServiceImpl<SdpsDirExpireMapper, SdpsDirExpire> implements ISdpsDirExpireService {
    @Autowired
    private SdpsDirExpireMapper dirExpireMapper;

    @Override
    public SdpsDirExpire selectByPath(Integer clusterId, String path) {
        QueryWrapper<SdpsDirExpire> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cluster_id", clusterId)
                .eq("path", path);

        return dirExpireMapper.selectOne(queryWrapper);
    }

    @Override
    public void updateExpire(StorageManageRequest request) {
        //检查参数
        checkUpdateParam(request);
        //查询表中已存在的记录
        QueryWrapper<SdpsDirExpire> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cluster_id", request.getClusterId())
                .in("path", request.getPaths());
        List<SdpsDirExpire> existList = dirExpireMapper.selectList(queryWrapper);
        List<String> existPath = existList.stream().map(x -> x.getPath()).collect(Collectors.toList());
        //计算出需要新增的路径
        List<String> newList = Lists.newArrayList(CollectionUtils.subtract(request.getPaths(), existPath).iterator());

        Date updateTime = new Date();
        //批量更新
        if (!existPath.isEmpty()) {
            UpdateWrapper<SdpsDirExpire> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("update_time", updateTime)
                    .set("expire_day", request.getExpireDay())
                    .eq("cluster_id", request.getClusterId())
                    .in("path", existPath);
            dirExpireMapper.update(null, updateWrapper);
        }
        //批量新增
        if (!newList.isEmpty()) {
            List<SdpsDirExpire> insertList = newList.stream().map(path -> {
                SdpsDirExpire sdpsDirExpire = new SdpsDirExpire();
                sdpsDirExpire.setExpireDay(request.getExpireDay());
                sdpsDirExpire.setClusterId(request.getClusterId());
                sdpsDirExpire.setPath(path);
                sdpsDirExpire.setUpdateTime(updateTime);
                return sdpsDirExpire;
            }).collect(Collectors.toList());
            dirExpireMapper.insertBatchSomeColumn(insertList);
        }

    }

    @Override
    public List<SdpsDirExpire> getDirExpire() {
        return dirExpireMapper.getCleanDir();
    }

    @Override
    public List<SdpsDirExpire> selectByPathList(Integer clusterId, List<String> pathList) {
        QueryWrapper<SdpsDirExpire> wrapper = new QueryWrapper<>();
        wrapper.eq("cluster_id", clusterId);
        if (!CollectionUtils.isEmpty(pathList)) {
            wrapper.in("path", pathList);
            return dirExpireMapper.selectList(wrapper);
        } else {
            return new ArrayList<>();
        }

    }

    /**
     * 检查参数
     * @param request
     */
    private void checkUpdateParam(StorageManageRequest request) {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(request.getClusterId());
        Preconditions.checkNotNull(request.getExpireDay());
        Preconditions.checkArgument(!CollectionUtils.isEmpty(request.getPaths()));
    }
}
