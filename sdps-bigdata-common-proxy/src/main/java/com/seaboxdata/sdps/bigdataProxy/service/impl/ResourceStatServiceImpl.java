package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.seaboxdata.sdps.bigdataProxy.feign.SeaBoxFeignService;
import com.seaboxdata.sdps.bigdataProxy.mapper.HdfsFileStatsMapper;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterDevOpsService;
import com.seaboxdata.sdps.bigdataProxy.service.IResourceStatService;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.utils.MathUtil;
import com.seaboxdata.sdps.common.framework.bean.dto.DirInfoDTO;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFileStats;
import com.seaboxdata.sdps.common.framework.bean.dto.TopDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.FileStatsDTO;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;
import com.seaboxdata.sdps.common.framework.enums.QueryEnum;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ResourceStatServiceImpl implements IResourceStatService {
    @Autowired
    SeaBoxFeignService seaBoxFeignService;

    @Autowired
    IClusterDevOpsService clusterDevOpsService;

    @Autowired
    HdfsFileStatsMapper hdfsFileStatsMapper;
    @Override
    public Result getStorageTopN(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            List<TopDTO> dirInfoDTOS = seaBoxFeignService.topN(dirRequest.getClusterId(), dirRequest.getPeriod(), dirRequest.getType(), dirRequest.getTopNum());
            log.info("getStorageTopN:{}", dirInfoDTOS);
            result.setData(dirInfoDTOS);
        } catch (Exception e) {
            log.error("查询topN报错", e);
            result.setCode(1);
            result.setMsg("查询topN报错");
        }
        return result;
    }

    @Override
    public Result getResourceStatByPage(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            IPage<DirInfoDTO> dirInfoDTOS = seaBoxFeignService.getResourceStatByPage(dirRequest.getClusterId(), dirRequest.getDurationDay(), dirRequest.getOrderColumn(), dirRequest.getPageSize(), dirRequest.getPageNo(), dirRequest.getDesc());
            log.info("getResourceStatByPage:{}", dirInfoDTOS);
            result.setData(dirInfoDTOS);
        } catch (Exception e) {
            log.error("查询存储情况列表报错", e);
            result.setCode(1);
            result.setMsg("查询存储情况列表报错");
        }
        return result;
    }

    @Override
    public Result getResourceByTenant(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            List<DirInfoDTO> dirInfoDTOS = seaBoxFeignService.getResourceByTenant(dirRequest.getClusterId(), dirRequest.getDurationDay(), dirRequest.getOrderColumn(), dirRequest.getTenant(), dirRequest.getDesc());
            log.info("getResourceStatByPage:{}", dirInfoDTOS);
            result.setData(dirInfoDTOS);
        } catch (Exception e) {
            log.error("查询存储情况列表报错", e);
            result.setCode(1);
            result.setMsg("查询存储情况列表报错");
        }
        return result;
    }

    @Override
    public Result selectStorageTrend(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            List<DirInfoDTO> dirInfoDTOS = seaBoxFeignService.selectStorageTrend(dirRequest.getClusterId(), dirRequest.getStartDay(), dirRequest.getEndDay(), dirRequest.getPath(), dirRequest.getStorageType().name(), dirRequest.getDbName(), dirRequest.getTable(), dirRequest.getCategory());
            log.info("selectStorageTrend:{}", dirInfoDTOS);
            result.setData(dirInfoDTOS);
        } catch (Exception e) {
            log.error("询存储资源变化趋势报错", e);
            result.setCode(1);
            result.setMsg("询存储资源变化趋势报错");
        }
        return result;
    }

    @Override
    public Result selectPathSelections(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            List<String> dirInfoDTOS = seaBoxFeignService.selectPathSelections(dirRequest.getClusterId(), dirRequest.getPath(), QueryEnum.PATH.name());
            log.info("selectStorageTrend:{}", dirInfoDTOS);
            result.setData(dirInfoDTOS);
        } catch (Exception e) {
            log.error("查询子路径报错", e);
            result.setCode(1);
            result.setMsg("查询子路径报错");
        }
        return result;
    }

    @Override
    public Result selectDatabaseSelections(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            List<String> dirInfoDTOS = seaBoxFeignService.selectDatabaseSelections(dirRequest.getClusterId(), QueryEnum.DB.name(), dirRequest.getDbName());
            log.info("selectDatabaseSelections:{}", dirInfoDTOS);
            result.setData(dirInfoDTOS);
        } catch (Exception e) {
            log.error("查询库列表报错", e);
            result.setCode(1);
            result.setMsg("查询库列表报错");
        }
        return result;
    }

    @Override
    public Result selectTableSelections(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            List<String> dirInfoDTOS = seaBoxFeignService.selectTableSelections(dirRequest.getClusterId(), QueryEnum.TABLE.name(), dirRequest.getDbName(), dirRequest.getTable(), dirRequest.getSubPath());
            log.info("selectTableSelections:{}", dirInfoDTOS);
            result.setData(dirInfoDTOS);
        } catch (Exception e) {
            log.error("查询表报错", e);
            result.setCode(1);
            result.setMsg("查询表报错");
        }
        return result;
    }

    @Override
    public Result selectDiffStorage(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            DirInfoDTO dirInfoDTO = seaBoxFeignService.selectDiffStorage(dirRequest.getClusterId(), dirRequest.getStartDay(), dirRequest.getEndDay(), dirRequest.getStorageType().name(), dirRequest.getPath(), dirRequest.getDbName(), dirRequest.getTable(), dirRequest.getCategory());
            log.info("selectDiffStorage:{}", dirInfoDTO);
            result.setData(dirInfoDTO);
        } catch (Exception e) {
            log.error("存储地图->查询存储量报错", e);
            result.setCode(1);
            result.setMsg("存储地图->查询存储量报错");
        }
        return result;
    }

    @Override
    public Result selectStorageRank(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            List<DirInfoDTO> list = seaBoxFeignService.selectStorageRank(dirRequest.getClusterId(), dirRequest.getStorageType().name(), dirRequest.getType(), dirRequest.getPath(), dirRequest.getPathDepth(), dirRequest.getDbName(), dirRequest.getCategory(), dirRequest.getTopNum());
            log.info("selectStorageRank:{}", list);
            result.setData(list);
        } catch (Exception e) {
            log.error("存储地图->排行报错", e);
            result.setCode(1);
            result.setMsg("存储地图->排行报错");
        }
        return result;
    }

    @Override
    public Result getFsContent(DirRequest dirRequest) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(dirRequest.getClusterId()));
        try {
            HdfsFSObj fsContent = seaBoxFeignService.getFsContent(dirRequest.getClusterId(), dirRequest.getPath());
            log.info("getFsContent:{}", fsContent);
            result.setData(fsContent);
        } catch (Exception e) {
            log.error("获取hdfs已使用容量和总容量报错", e);
            result.setCode(1);
            result.setMsg("获取hdfs已使用容量和总容量报错");
        }
        return result;
    }

    @Override
    public Result getStatsByType(Integer clusterId, String type) {
        Result result = new Result();
        result.setCode(0);
        //判断是否有集群可用
        clusterDevOpsService.getEnableClusterList(Lists.list(clusterId));
        try {
            List<FileStatsDTO> dtos = Lists.newArrayList();
            List<HdfsFileStats> stats = hdfsFileStatsMapper.selectByType(clusterId, type);
            Long totalFileNum = 0L;
            Long totalFileSize = 0L;
            //计算总量
            for (HdfsFileStats dto : stats) {
                totalFileSize += dto.getTypeValueSize();
                totalFileNum += dto.getTypeValueNum();
            }
            //计算百分比
            for (HdfsFileStats dto : stats) {
                FileStatsDTO statsDTO = new FileStatsDTO();
                BeanUtils.copyProperties(dto, statsDTO);
                statsDTO.setPercentNum(MathUtil.divisionToPercent(dto.getTypeValueNum(), totalFileNum));
                statsDTO.setPercentSize(MathUtil.divisionToPercent(dto.getTypeValueSize(), totalFileSize));
                dtos.add(statsDTO);
            }
            log.info("getStatsByType:{}", dtos);
            result.setData(dtos);
        } catch (Exception e) {
            log.error("查询数据报错", e);
            result.setCode(1);
            result.setMsg("查询数据报错");
        }
        return result;
    }
}
