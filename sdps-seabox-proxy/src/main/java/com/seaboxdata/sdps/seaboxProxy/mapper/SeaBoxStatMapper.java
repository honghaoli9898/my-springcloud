package com.seaboxdata.sdps.seaboxProxy.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.dto.DirInfoDTO;
import com.seaboxdata.sdps.common.framework.bean.StorgeDirInfo;
import com.seaboxdata.sdps.common.framework.bean.TbDirInfo;
import com.seaboxdata.sdps.common.framework.bean.dto.TopDTO;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;

/**
 * 这里DS注解，p0代表第一个参数 p1代表第二个参数
 */
public interface SeaBoxStatMapper extends SuperMapper<TbDirInfo> {

	@DS("#p1")
	com.github.pagehelper.Page<StorgeDirInfo> getItemStorage(
			@Param("storgeRequest") StorgeRequest storgeRequest, String datasourceKey);
    @DS("#p1")
    List<TopDTO> getTenantTopN(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<String> getTopNTentant(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    IPage<DirInfoDTO> getTopNResource(@Param("dirRequest") DirRequest dirRequest, String datasourceKey, Page<?> page);
    @DS("#p1")
    List<DirInfoDTO> getDiffResource(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<DirInfoDTO> getResourceByTenant(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<DirInfoDTO> getDiffResourceByTenant(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<DirInfoDTO> selectPathTrend(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<DirInfoDTO> selectDBSumTrendInType(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<StorgeDirInfo> getFileStorageByTenant(@Param("storgeRequest")StorgeRequest storgeRequest,
			String datasourceKey);
    @DS("#p1")
    List<String> selectPathByParentPath(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<DirInfoDTO> selectDatabase(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<DirInfoDTO> selectTables(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    com.github.pagehelper.Page<StorgeDirInfo> selectSubPathTrend(@Param("storgeRequest")StorgeRequest storgeRequest, String datasourceKey);
    @DS("#p1")
    DirInfoDTO selectFileSizeByPath(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    DirInfoDTO selectFileSizeByDatabase(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    DirInfoDTO selectFileSizeByTable(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<DirInfoDTO> selectRankByPath(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p1")
    List<DirInfoDTO> selectRankByDB(@Param("dirRequest") DirRequest dirRequest, String datasourceKey);
    @DS("#p0")
    String selectLatestDay(String datasourceKey, @Param("dayTime") String dayTime);
}
