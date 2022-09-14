package com.seaboxdata.sdps.job.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.job.admin.core.model.SdpsCluster;
import com.seaboxdata.sdps.job.admin.core.model.SdpsClusterExample;
@Mapper
public interface SdpsClusterDao {

    long countByExample(SdpsClusterExample example);

    int deleteByExample(SdpsClusterExample example);

    int deleteByPrimaryKey(@Param("clusterId") Integer clusterId, @Param("clusterPasswd") String clusterPasswd);

    int insert(SdpsCluster record);

    int insertSelective(SdpsCluster record);

    List<SdpsCluster> selectByExample(SdpsClusterExample example);

    SdpsCluster selectByPrimaryKey(@Param("clusterId") Integer clusterId, @Param("clusterPasswd") String clusterPasswd);

    int updateByExampleSelective(@Param("record") SdpsCluster record, @Param("example") SdpsClusterExample example);

    int updateByExample(@Param("record") SdpsCluster record, @Param("example") SdpsClusterExample example);

    int updateByPrimaryKeySelective(SdpsCluster record);

    int updateByPrimaryKey(SdpsCluster record);
}