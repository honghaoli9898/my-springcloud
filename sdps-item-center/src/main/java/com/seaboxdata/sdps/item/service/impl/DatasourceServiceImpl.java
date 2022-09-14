package com.seaboxdata.sdps.item.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.utils.KeytabUtil;
import com.seaboxdata.sdps.item.enums.DbTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.item.anotation.DataPermission;
import com.seaboxdata.sdps.item.dto.datasource.DataSourceDto;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.enums.DbSourcrTypeEnum;
import com.seaboxdata.sdps.item.mapper.DatasourceItemMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceParamMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceTypeMapper;
import com.seaboxdata.sdps.item.model.DatasourceItem;
import com.seaboxdata.sdps.item.model.SdpsDatasource;
import com.seaboxdata.sdps.item.model.SdpsDatasourceParam;
import com.seaboxdata.sdps.item.model.SdpsDatasourceType;
import com.seaboxdata.sdps.item.service.IDatasourceService;
import com.seaboxdata.sdps.item.service.IItemService;
import com.seaboxdata.sdps.item.vo.datasource.DataSourceRequest;

@Service
public class DatasourceServiceImpl extends
		SuperServiceImpl<SdpsDatasourceMapper, SdpsDatasource> implements
		IDatasourceService {
	@Autowired
	private IItemService itemService;
	@Autowired
	private SdpsDatasourceParamMapper datasourceParamMapper;
	@Autowired
	private SdpsDatasourceTypeMapper datasourceTypeMapper;
	@Autowired
	private DatasourceItemMapper datasourceItemMapper;

	@Autowired
	DynamicOperatorDataSourceService dynamicOperatorDataSourceService;

	@Override
	public List<SdpsDatasourceType> findDataSourceType(DataSourceRequest request) {
		List<SdpsDatasourceType> results = datasourceTypeMapper
				.selectDataSourceType(request.getCategory(),
						request.getIsValid());
		return results;
	}

	@Override
	@Transactional
	public void saveDataSourceType(SdpsDatasource datasource) throws Exception {
		datasource.setIsValid(true);
		datasource.setIsVisible(true);
		datasource.setSystemInit(false);
		this.save(datasource);
		insertDatasourceItemByIds(datasource.getId(), datasource.getItemIds());
		SdpsDatasourceType datasourceType = datasourceTypeMapper
				.selectById(datasource.getTypeId());
		dynamicOperatorDataSourceService.add(datasource.getName(),
				datasourceType.getName(), datasource.getProperties());
	}

	@Override
	public PageResult<DataSourceDto> findDatasourcePage(
			PageRequest<DataSourceRequest> request) {
		if (!Objects.equals(request.getParam().getUsername(), "admin")) {
			List<ItemDto> itemDtos = itemService.selectItemByUser(
					Long.valueOf(request.getParam().getUserId()), false);
			Set<Long> itemIds = itemDtos.stream().map(ItemDto::getId)
					.collect(Collectors.toSet());

			StringBuffer sb = new StringBuffer();
			sb.append("si.id").append(" in(");
			itemIds.forEach(id -> {
				sb.append("'").append(id).append("',");
			});
			if (CollUtil.isNotEmpty(itemIds)) {
				sb.deleteCharAt(sb.length() - 1);
			} else {
				sb.append("''");
			}
			sb.append(")");
			if (CollUtil.isNotEmpty(itemIds)) {
				List<DataSourceDto> clusterId = this.baseMapper
						.selectClusterIdAssItem(itemIds);
				if(CollUtil.isNotEmpty(clusterId)){
					sb.append(" or sd.cluster_id in (");
					clusterId.forEach(id -> {
						sb.append("'").append(id.getClusterId()).append("',");
					});
					if (CollUtil.isNotEmpty(clusterId)) {
						sb.deleteCharAt(sb.length() - 1);
					} else {
						sb.append("''");
					}
					sb.append(")");
				}
			}
			request.getParam().setDataPermissionSql(sb.toString());
		}
		PageHelper.startPage(request.getPage(), request.getSize());
		Page<DataSourceDto> dataSources = baseMapper.selectDatasource(request
				.getParam());
		return PageResult.<DataSourceDto> builder().code(0)
				.data(dataSources.getResult()).count(dataSources.getTotal())
				.msg("操作成功").build();
	}

	@Override
	public List<SdpsDatasourceParam> findDatasourceParamByType(
			DataSourceRequest request) {
		return datasourceParamMapper
				.selectList(new QueryWrapper<SdpsDatasourceParam>().eq(
						"type_id", request.getId()));
	}

	@Override
	@Transactional
	public void updateDatasourceById(SdpsDatasource datasource)
			throws Exception {
		SdpsDatasourceType datasourceType = datasourceTypeMapper
				.selectById(datasource.getTypeId());
		if (DbSourcrTypeEnum.cluster.name().equalsIgnoreCase(
				datasourceType.getCategory())) {
			throw new BusinessException("系统数据源不能修改");
		}
		if (CollUtil.isNotEmpty(datasource.getItemIds())) {
			datasourceItemMapper.delete(new QueryWrapper<DatasourceItem>().eq(
					"datasource_id", datasource.getId()));
			insertDatasourceItemByIds(datasource.getId(),
					datasource.getItemIds());
		}
		this.updateById(datasource);
		SdpsDatasource oldDatasource = this.getById(datasource.getId());
		dynamicOperatorDataSourceService.remove(oldDatasource.getName());
		dynamicOperatorDataSourceService.add(datasource.getName(),
				datasourceType.getName(), datasource.getProperties());
	}

	private void insertDatasourceItemByIds(Long datasourceId, List<Long> itemIds) {
		datasourceItemMapper.insertBatchSomeColumn(itemIds.stream().map(id -> {
			DatasourceItem datasourceItem = new DatasourceItem();
			datasourceItem.setItemId(id);
			datasourceItem.setDatasourceId(datasourceId);
			return datasourceItem;
		}).collect(Collectors.toList()));
	}

	@Override
	@Transactional
	public void deleteDatasourceByIds(DataSourceRequest request) {
		List<SdpsDatasource> list = this
				.list(new QueryWrapper<SdpsDatasource>().select("name").in(
						"id", request.getIds()));
		this.removeByIds(request.getIds());
		datasourceItemMapper.deleteBatchIds(request.getIds());
		list.stream().map(SdpsDatasource::getName).forEach(name -> {
			dynamicOperatorDataSourceService.remove(name);
		});
	}

	@Override
	@DataPermission(joinName = "si.id")
	public Result<List<DataSourceDto>> findDatasource(DataSourceRequest request) {
		Page<DataSourceDto> dataSources = baseMapper.selectDatasource(request);
		return Result.succeed(dataSources.getResult(), "操作成功");
	}

	@Override
	public Result<List<SdpsDatasourceType>> selectDataSourceType(
			DataSourceRequest request) {
		QueryWrapper<SdpsDatasourceType> wrapper = new QueryWrapper<SdpsDatasourceType>();
		if (StrUtil.isNotBlank(request.getCategory())) {
			wrapper.eq("category", request.getCategory());
		}
		if (Objects.nonNull(request.getIsValid())) {
			wrapper.eq("is_valid", request.getIsValid());
		}
		List<SdpsDatasourceType> result = datasourceTypeMapper
				.selectList(wrapper);
		return Result.succeed(result, "操作成功");
	}

	@Override
	public String getHivePrincipal(Integer clusterId) {
		Long typeId = datasourceTypeMapper.selectOne(
				new QueryWrapper<SdpsDatasourceType>().select("id").eq(
						"name", DbTypeEnum.H.getCode())
						.eq("category", "cluster")).getId();
		SdpsDatasource sdpsDatasource = this.baseMapper
				.selectOne(new QueryWrapper<SdpsDatasource>().eq(
						"cluster_id", clusterId).eq(
						"type_id", typeId));
		JSONObject jsonObject = JSONObject.parseObject(sdpsDatasource.getProperties());
		return jsonObject.getString("username");
	}

	@Override
	public void updateKeytabs(List<SdpServerKeytab> list) {
		KeytabUtil.updateKeytabs(list);
		List<Integer> clusterIds = Lists.newArrayList();
		for (SdpServerKeytab keytab : list) {
			if ("SDPS".equals(keytab.getPrincipalType())) {
				continue;
			}
			clusterIds.add(keytab.getClusterId());
		}
		List<String> poolNames = baseMapper.selectKerberosHive(clusterIds);
		dynamicOperatorDataSourceService.restartDataSources(poolNames);
	}
}