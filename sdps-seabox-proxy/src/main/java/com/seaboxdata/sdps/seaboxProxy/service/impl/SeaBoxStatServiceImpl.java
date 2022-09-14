package com.seaboxdata.sdps.seaboxProxy.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.core.constant.DbConstants;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.core.utils.MathUtil;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.TbDirInfo;
import com.seaboxdata.sdps.common.framework.bean.dto.DirInfoDTO;
import com.seaboxdata.sdps.common.framework.bean.dto.TopDTO;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;
import com.seaboxdata.sdps.common.framework.enums.DirFileType;
import com.seaboxdata.sdps.common.framework.enums.HdfsStatTypeEnum;
import com.seaboxdata.sdps.common.framework.enums.QueryEnum;
import com.seaboxdata.sdps.common.utils.excelutil.DateUtil;
import com.seaboxdata.sdps.seaboxProxy.config.CommonConstraint;
import com.seaboxdata.sdps.seaboxProxy.config.DynamicDataSourceConfig;
import com.seaboxdata.sdps.seaboxProxy.mapper.SeaBoxStatMapper;
import com.seaboxdata.sdps.seaboxProxy.service.ISeaBoxStatService;
import com.seaboxdata.sdps.seaboxProxy.util.HdfsUtil;
import com.seaboxdata.sdps.seaboxProxy.util.SeaboxDateUtil;

@Service
public class SeaBoxStatServiceImpl extends SuperServiceImpl<SeaBoxStatMapper, TbDirInfo> implements ISeaBoxStatService {
    @Autowired
    private SeaBoxStatMapper seaBoxStatMapper;
    @Autowired
    private DynamicDataSourceConfig dynamicDataSourceConfig;

    @Override
    public List<TopDTO> topN(DirRequest dirRequest) {
        //检查参数
        checkParam(dirRequest);
        //设置类型
        dirRequest.setStorageTypeList(QueryEnum.getAllTypeIndex());

        String datasourceKey = dirRequest.getClusterId() + CommonConstraint.phoenix;
        dynamicDataSourceConfig.changePhoenixDataSource(dirRequest.getClusterId(), datasourceKey);

        //查询total
        List<TopDTO> total = getTenantTopN(dirRequest, true, datasourceKey);
        //查询topN
        if (!total.isEmpty()) {
            //查询当天topN的租户
            List<String> tentants = seaBoxStatMapper.getTopNTentant(dirRequest, datasourceKey);
            if (!tentants.isEmpty()) {
                dirRequest.setTenants(tentants);
                //根据租户查询存储量topN
                List<TopDTO> tenantList = getTenantTopN(dirRequest, false, datasourceKey);
                //合并total结果和具体tenant结果
                total.addAll(tenantList);
            }
        }
        //租户集合
        ArrayList<String> list = new ArrayList<>();
        list.add("TOTAL");
        if (!CollectionUtils.isEmpty(dirRequest.getTenants())) {
            list.addAll(dirRequest.getTenants());
        }
        //list转map，将租户和顺序的对应关系存储起来
        Map<String, Integer> indexMap = Maps.newHashMap();
        for (int i = 0;i<list.size();i ++) {
            indexMap.put(list.get(i), i);
        }
        //时间范围
        Integer length = DateUtil.subDate(dirRequest.getStartDay(), dirRequest.getEndDay(), DateUtil.DATE_FORMAT_YYYYMMDD) + 1;
        TopDTO[] result = new TopDTO[length * list.size()];
        total.forEach(top -> {
            Integer index = indexMap.get(top.getTenant());
            Integer subLength = DateUtil.subDate(dirRequest.getStartDay(), top.getDayTime(), DateUtil.DATE_FORMAT_YYYYMMDD);
            result[index * length + subLength] = top;
        });
        //为空元素赋默认值
        for (int i = 0; i<result.length; i++) {
            if (result[i] == null) {
                //计算出属于哪个租户
                String tenant = list.get(i / length);
                //计算出日期
                int offset = i % length;
                String day = DateUtil.subDay(dirRequest.getStartDay(), -offset, DateUtil.DATE_FORMAT_YYYYMMDD);
                TopDTO topDTO = new TopDTO(0L, tenant, day);
                result[i] = topDTO;
            }
        }
        return Lists.newArrayList(result);
    }

    @Override
    public IPage<DirInfoDTO> getResourceStatByPage(DirRequest dirRequest) {

        checkParam(dirRequest);
        //设置类型
        dirRequest.setStorageTypeList(QueryEnum.getAllTypeIndex());
        String datasourceKey = dirRequest.getClusterId() + CommonConstraint.phoenix;
        dynamicDataSourceConfig.changePhoenixDataSource(dirRequest.getClusterId(), datasourceKey);

        Page<DirInfoDTO> page = new Page<>();
        page.setSize(dirRequest.getPageSize());
        page.setCurrent((long)dirRequest.getPageNo());
        IPage<DirInfoDTO> dirInfoIPage = seaBoxStatMapper.getTopNResource(dirRequest, datasourceKey, page);

        if (!CollectionUtils.isEmpty(dirInfoIPage.getRecords())) {
            List<DirInfoDTO> dirInfoList = dirInfoIPage.getRecords();

            dirInfoList = dirInfoList.stream().map(dirInfo -> {
                dirInfo.setTotalFileSize(dirInfo.getSumTotalFileSize());
                dirInfo.setTotalFileNum(dirInfo.getSumTotalFileNum());
                dirInfo.setTotalSmallFileNum(dirInfo.getSumTotalSmallFileNum());
                return dirInfo;
            }).collect(Collectors.toList());

            List<String> tenantList = dirInfoList.stream().map(dirInfo -> dirInfo.getTenant()).distinct().collect(Collectors.toList());
            dirRequest.setTenants(tenantList);

            //查询对比日期的存储信息
            List<DirInfoDTO> diffResource = seaBoxStatMapper.getDiffResource(dirRequest, datasourceKey);

            packageDirInfo(dirInfoList, diffResource);
        }



        return dirInfoIPage;
    }

    private void packageDirInfo(List<DirInfoDTO> dirInfoList, List<DirInfoDTO> diffResource) {
        diffResource.forEach(diff -> {
            DirInfoDTO tbDirInfo = dirInfoList.stream().filter(dirInfo -> diff.getTenant().equals(dirInfo.getTenant())).findFirst().orElse(new DirInfoDTO());
            //计算差值和增长率
            long diffFileSize = tbDirInfo.getSumTotalFileSize() - diff.getTotalFileSize();
            long diffFileNum = tbDirInfo.getSumTotalFileNum() - diff.getTotalFileNum();
            long diffSmallFileNum = tbDirInfo.getSumTotalSmallFileNum() - diff.getTotalSmallFileNum();
            tbDirInfo.setDiffTotalFileSize(diffFileSize);
            tbDirInfo.setDiffTotalFileNum(diffFileNum);
            tbDirInfo.setDiffTotalSmallFileNum(diffSmallFileNum);
            tbDirInfo.setDiffTotalFileSizeRatio(MathUtil.divisionToPercent(diffFileSize, tbDirInfo.getSumTotalFileSize()));
            tbDirInfo.setDiffTotalFileNumRatio(MathUtil.divisionToPercent(diffFileNum, tbDirInfo.getSumTotalFileNum()));
            tbDirInfo.setDiffTotalSmallFileNumRatio(MathUtil.divisionToPercent(diffSmallFileNum, tbDirInfo.getSumTotalSmallFileNum()));
        });
    }

    @Override
    public List<DirInfoDTO> getResourceByTenant(DirRequest dirRequest) {
        checkParam(dirRequest);
        String datasourceKey = dirRequest.getClusterId() + CommonConstraint.phoenix;
        dynamicDataSourceConfig.changePhoenixDataSource(dirRequest.getClusterId(), datasourceKey);

        List<DirInfoDTO> currentResource = seaBoxStatMapper.getResourceByTenant(dirRequest, datasourceKey);
        List<DirInfoDTO> diffResource = seaBoxStatMapper.getDiffResourceByTenant(dirRequest, datasourceKey);
        packageDirInfo(currentResource, diffResource);

        return currentResource;
    }

    @Override
    public List<DirInfoDTO> selectStorageTrend(DirRequest dirRequest) {
        Preconditions.checkNotNull(dirRequest);
        String datasourceKey = dirRequest.getClusterId() + CommonConstraint.phoenix;
        dynamicDataSourceConfig.changePhoenixDataSource(dirRequest.getClusterId(), datasourceKey);

        List<DirInfoDTO> result = Lists.newArrayList();
        if (QueryEnum.PATH.equals(dirRequest.getStorageType())) {
            checkPathParam(dirRequest);
            result = seaBoxStatMapper.selectPathTrend(dirRequest, datasourceKey);
        } else if (QueryEnum.DB.equals(dirRequest.getStorageType()) || QueryEnum.TABLE.equals(dirRequest.getStorageType())) {
            checkDbParam(dirRequest);
            result = seaBoxStatMapper.selectDBSumTrendInType(dirRequest, datasourceKey);
        }
        //时间范围
        Integer length = DateUtil.subDate(dirRequest.getStartDay(), dirRequest.getEndDay(), DateUtil.DATE_FORMAT_YYYYMMDD) + 1;
        DirInfoDTO[] array = new DirInfoDTO[length];
        result.forEach(info -> {
            Integer subLength = DateUtil.subDate(dirRequest.getStartDay(), info.getDayTime(), DateUtil.DATE_FORMAT_YYYYMMDD);
            array[subLength] = info;
        });
        //空元素赋默认值
        for (int i=0;i<array.length;i++) {
            if (array[i] == null) {
                String dayTime = DateUtil.subDay(dirRequest.getStartDay(), -i, DateUtil.DATE_FORMAT_YYYYMMDD);
                DirInfoDTO info = new DirInfoDTO();
                info.setDayTime(dayTime);
                info.setTotalFileSize(0L);
                info.setTotalBlockNum(0L);
                info.setTotalFileNum(0L);
                info.setTotalSmallFileNum(0L);
                info.setTotalEmptyFileNum(0L);
                array[i] = info;
            }
        }
        return Lists.newArrayList(array);
    }

    @Override
    public List<String> selectStorageSelections(DirRequest dirRequest) {
        List<String> result = Lists.newArrayList();
        Preconditions.checkNotNull(dirRequest);
        //获取封装集群名
//        getClusterName(dirRequest);
        //获取动态数据源
        String datasourceKey = dirRequest.getClusterId() + CommonConstraint.phoenix;
        dynamicDataSourceConfig.changePhoenixDataSource(dirRequest.getClusterId(), datasourceKey);

        if (QueryEnum.PATH.equals(dirRequest.getStorageType())) {
            //路径类型
            result = selectPathByParentPath(dirRequest, datasourceKey);
        } else if (QueryEnum.DB.equals(dirRequest.getStorageType())) {
            //库类型
            result = selectDatabase(dirRequest, datasourceKey);
        } else if (QueryEnum.TABLE.equals(dirRequest.getStorageType())) {
            //表类型
            result = selectTables(dirRequest, datasourceKey);
        }
        return result;
    }

    @Override
    public DirInfoDTO selectDiffStorage(DirRequest dirRequest) {
        Preconditions.checkNotNull(dirRequest);
        //如果当前时间没有统计信息，则获取之前最新一天的统计信息
        dirRequest.setGetCurrentDate(true);

        String datasourceKey = dirRequest.getClusterId() + CommonConstraint.phoenix;
        dynamicDataSourceConfig.changePhoenixDataSource(dirRequest.getClusterId(), datasourceKey);

        String startDay = dirRequest.getStartDay();
        DirInfoDTO result = new DirInfoDTO();
        if (QueryEnum.PATH.equals(dirRequest.getStorageType())) {
            //hdfs路径类型
            checkPathParam(dirRequest);
            //查询出开始日期的存储量
            result = seaBoxStatMapper.selectFileSizeByPath(dirRequest, datasourceKey);
            //查询出结束日期的存储量
            dirRequest.setStartDay(dirRequest.getEndDay());
            DirInfoDTO endResult = seaBoxStatMapper.selectFileSizeByPath(dirRequest, datasourceKey);
            result = packageDiffDirInfo(result, endResult, startDay, dirRequest);
        } else if (QueryEnum.DB.equals(dirRequest.getStorageType())) {
            //库类型
            checkDbParam(dirRequest);
            //查询出开始日期的存储量
            result = seaBoxStatMapper.selectFileSizeByDatabase(dirRequest, datasourceKey);
            //查询出结束日期的存储量
            dirRequest.setStartDay(dirRequest.getEndDay());
            DirInfoDTO endResult = seaBoxStatMapper.selectFileSizeByDatabase(dirRequest, datasourceKey);
            result = packageDiffDirInfo(result, endResult, startDay, dirRequest);
        } else if (QueryEnum.TABLE.equals(dirRequest.getStorageType())) {
            //表类型
            checkDbParam(dirRequest);
            //查询出开始日期的存储量
            result = seaBoxStatMapper.selectFileSizeByTable(dirRequest, datasourceKey);
            //查询出结束日期的存储量
            dirRequest.setStartDay(dirRequest.getEndDay());
            DirInfoDTO endResult = seaBoxStatMapper.selectFileSizeByTable(dirRequest, datasourceKey);
            result = packageDiffDirInfo(result, endResult, startDay, dirRequest);
        }
        return result;
    }

    @Override
    public List<DirInfoDTO> selectStorageRank(DirRequest dirRequest) {
        Preconditions.checkNotNull(dirRequest);
        String datasourceKey = dirRequest.getClusterId() + CommonConstraint.phoenix;
        dynamicDataSourceConfig.changePhoenixDataSource(dirRequest.getClusterId(), datasourceKey);
        //设置默认参数
        checkParam(dirRequest);
        String toDay = DateUtil.subDay(new Date(), dirRequest.getDurationDay(), DateUtil.DATE_FORMAT_YYYYMMDD);
        String latestDay = seaBoxStatMapper.selectLatestDay(datasourceKey, toDay);
        if (StringUtils.isNotBlank(latestDay)) {
            dirRequest.setEndDay(latestDay);
        }
        //设置排序字段
        dirRequest.setOrderColumn(dirRequest.getMetric());
        List<DirInfoDTO> result = Lists.newArrayList();
        if (QueryEnum.PATH.equals(dirRequest.getStorageType())) {
            Integer pathDepth = dirRequest.getPathDepth() == null ? 1 : dirRequest.getPathDepth();
            //设置路径深度
            dirRequest.setPathIndex(dirRequest.getPathIndex() + pathDepth);
            //判断路径
            if ("/".equals(dirRequest.getPath())) {
                dirRequest.setPath("");
            }
            result = seaBoxStatMapper.selectRankByPath(dirRequest, datasourceKey);
        } else if (QueryEnum.DB.equals(dirRequest.getStorageType()) || QueryEnum.TABLE.equals(dirRequest.getStorageType())) {
            //设置查询类型
            List<Integer> types = QueryEnum.getTypeIndex(dirRequest.getStorageType(), dirRequest.getCategory());
            dirRequest.setStorageTypeList(types);
            result = seaBoxStatMapper.selectRankByDB(dirRequest, datasourceKey);
        }
        return result;
    }

    @Override
    public HdfsFSObj getFsContent(DirRequest dirRequest) {
        Preconditions.checkNotNull(dirRequest);
        //设置默认路径
        if (StringUtils.isBlank(dirRequest.getPath())) {
            dirRequest.setPath("/");
        }
        HdfsUtil hdfsUtil = new HdfsUtil(dirRequest.getClusterId());
        return hdfsUtil.getStorageSize(dirRequest.getPath());
    }

    /**
     * 将开始日期的存储量封装到结束日期存储量中
     * @param dirInfo
     * @param diffDirInfo
     */
    private DirInfoDTO packageDiffDirInfo(DirInfoDTO dirInfo, DirInfoDTO diffDirInfo, String startDay, DirRequest dirRequest) {
        //检查参数
        if (dirInfo == null) {
            dirInfo = new DirInfoDTO();
            dirInfo.setSumTotalFileSize(0L);
        }
        if (diffDirInfo == null) {
            diffDirInfo = new DirInfoDTO();
            diffDirInfo.setSumTotalFileSize(0L);
        }
        Long sumTotalFileSize = 0L;
        if (dirInfo.getSumTotalFileSize() != null) {
            sumTotalFileSize = dirInfo.getSumTotalFileSize();
        } else {
            dirInfo.setSumTotalFileSize(sumTotalFileSize);
        }
        Long diffSumTotalFileSize = diffDirInfo.getSumTotalFileSize() == null ? 0L : diffDirInfo.getSumTotalFileSize();

        dirInfo.setDiffTotalFileSize(diffDirInfo.getTotalFileSize());
        long diffFileSize = diffSumTotalFileSize - sumTotalFileSize;
        dirInfo.setDiffTotalFileSizeRatio(MathUtil.divisionToPercent(diffFileSize, diffSumTotalFileSize));
        dirInfo.setDiffTotalFileSize(diffSumTotalFileSize);
        dirInfo.setDiffDay(startDay);
        dirInfo.setDayTime(dirRequest.getEndDay());

        return dirInfo;
    }

    /**
     * 查询所有表
     * @param dirRequest
     * @param datasourceKey 动态数据库对应的key
     * @return
     */
    private List<String> selectTables(DirRequest dirRequest, String datasourceKey) {
        //设置表对应的类型集合
        dirRequest.setStorageTypeList(QueryEnum.getTableIndex());

        List<DirInfoDTO> dirInfoDTOS = seaBoxStatMapper.selectTables(dirRequest, datasourceKey);
        return dirInfoDTOS.stream().map(dirInfo -> {
            String table = dirInfo.getTypeValue();
            String type = "";
            if (DirFileType.TABLE_HIVE.getIndex().equals(dirInfo.getType())) {
                type = DbConstants.HIVE;
            } else if (DirFileType.TABLE_HBASE.getIndex().equals(dirInfo.getType())) {
                type = DbConstants.HBASE;
            } else if (DirFileType.DATABASE_EXTERNAL_HIVE.getIndex().equals(dirInfo.getType())) {
                type = DbConstants.HIVE_EXTERNAL;
            }
            return table + "(" + type + ")";
        }).collect(Collectors.toList());
    }

    /**
     * 查询所有库
     * @param dirRequest
     * @param datasourceKey 动态数据库对应的key
     * @return
     */
    private List<String> selectDatabase(DirRequest dirRequest, String datasourceKey) {
        //设置库对应的类型集合
        dirRequest.setStorageTypeList(QueryEnum.getDBIndex());

        List<DirInfoDTO> dirInfoDTOS = seaBoxStatMapper.selectDatabase(dirRequest, datasourceKey);
        return dirInfoDTOS.stream().map(dirInfo -> {
            String database = dirInfo.getTypeValue();
            String type = "";
            if (DirFileType.DATABASE_HIVE.getIndex().equals(dirInfo.getType())) {
                type = DbConstants.HIVE;
            } else if (DirFileType.DATABASE_HBASE.getIndex().equals(dirInfo.getType())) {
                type = DbConstants.HBASE;
            } else if (DirFileType.DATABASE_EXTERNAL_HIVE.getIndex().equals(dirInfo.getType())) {
                type = DbConstants.HIVE_EXTERNAL;
            }
            return database + "(" + type + ")";
        }).collect(Collectors.toList());
    }

    /**
     * 根据父目录查询子目录
     * @param dirRequest
     * @param datasourceKey 动态数据库对应的key
     * @return
     */
    public List<String> selectPathByParentPath(DirRequest dirRequest, String datasourceKey) {
        if (StringUtils.isBlank(dirRequest.getPath())) {
            dirRequest.setPath("/");
        }

        return seaBoxStatMapper.selectPathByParentPath(dirRequest, datasourceKey);
    }

    private void checkDbParam(DirRequest dirRequest) {
        computeDate(dirRequest);
        //根据类型获取type集合
        List<Integer> types = QueryEnum.getTypeIndex(dirRequest.getStorageType(), dirRequest.getCategory());
        dirRequest.setStorageTypeList(types);
    }

    private void checkPathParam(DirRequest dirRequest) {
        computeDate(dirRequest);
        //如果路径为空，则设置为根目录
        if (StringUtils.isBlank(dirRequest.getPath())) {
            dirRequest.setPath("/");
        }
    }

    /**
     * 查询时间范围内存储量或文件数或小文件数
     * @param dirRequest
     * @param isTotal    是否查总和
     * @return
     */
    private List<TopDTO> getTenantTopN(DirRequest dirRequest, Boolean isTotal, String datasourceKey) {
        dirRequest.setIsTotal(isTotal);
        List<TopDTO> total = seaBoxStatMapper.getTenantTopN(dirRequest, datasourceKey);
        if (isTotal) {
            total.forEach(t -> t.setTenant("TOTAL"));
        }
        return total;
    }

    /**
     * 检查参数、设置默认参数
     * @param dirRequest
     */
    private void checkParam(DirRequest dirRequest) {
        Preconditions.checkNotNull(dirRequest);

        if (dirRequest.getType() != null) {
            //设置维度字段
            if (dirRequest.getType() == 1) {
                dirRequest.setMetric("TOTAL_FILE_SIZE");
            } else if (dirRequest.getType() == 2) {
                dirRequest.setMetric("TOTAL_FILE_NUM");
            } else if (dirRequest.getType() == 3) {
                dirRequest.setMetric("TOTAL_SMALL_FILE_NUM");
            }
        }

        //根据查询类型设置默认排序列
        if (StringUtils.isBlank(dirRequest.getOrderColumn())) {
            String orderColumn = "TOTAL_FILE_SIZE";
            if (HdfsStatTypeEnum.FILE.name().equalsIgnoreCase(dirRequest.getOrderColumn())) {
                orderColumn = "TOTAL_FILE_NUM";
            } else if (HdfsStatTypeEnum.SMALLFILE.name().equalsIgnoreCase(dirRequest.getOrderColumn())) {
                orderColumn = "TOTAL_SMALL_FILE_NUM";
            }
            dirRequest.setOrderColumn(orderColumn);
        }
        if (StringUtils.isNotBlank(dirRequest.getPeriod())) {
            SeaboxDateUtil.setDayInfo(dirRequest);
        } else {
            computeDate(dirRequest);
        }
    }

    /**
     * 计算出起始时间范围
     * @param dirRequest
     */
    private void computeDate(DirRequest dirRequest) {
        Integer durationDay = dirRequest.getDurationDay();
        if (durationDay == null) {
            durationDay = 7;
            dirRequest.setDurationDay(durationDay);
        }
        String startDay = dirRequest.getStartDay();
        String endDay = dirRequest.getEndDay();
        //获取当前时间和查询开始时间
        if (StringUtils.isBlank(startDay)) {
            startDay = DateUtil.subDay(new Date(), dirRequest.getDurationDay(), DateUtil.DATE_FORMAT_YYYYMMDD);
        } else {
            startDay = getCurrentDate(dirRequest, startDay);
            if (StringUtils.isBlank(startDay)) {
                startDay = DateUtil.subDay(new Date(), dirRequest.getDurationDay(), DateUtil.DATE_FORMAT_YYYYMMDD);
            }
        }
        dirRequest.setStartDay(startDay);

        if (StringUtils.isBlank(endDay)) {
            endDay = DateUtil.subDay(new Date(), 1, DateUtil.DATE_FORMAT_YYYYMMDD);
        } else {
            endDay = getCurrentDate(dirRequest, endDay);
            if (StringUtils.isBlank(endDay)) {
                endDay = DateUtil.subDay(new Date(), 1, DateUtil.DATE_FORMAT_YYYYMMDD);
            }
        }
        dirRequest.setEndDay(endDay);

    }

    /**
     * 获取最大存在日期
     * @param dirRequest 集群id
     * @param date       日期
     * @return
     */
    private String getCurrentDate(DirRequest dirRequest, String date) {
        if (!dirRequest.getGetCurrentDate()) {
            return date;
        }
        String datasourceKey = dirRequest.getClusterId() + CommonConstraint.phoenix;
        String latestDay = seaBoxStatMapper.selectLatestDay(datasourceKey, date);
        return StringUtils.isBlank(latestDay) ? date : latestDay;
    }
}
