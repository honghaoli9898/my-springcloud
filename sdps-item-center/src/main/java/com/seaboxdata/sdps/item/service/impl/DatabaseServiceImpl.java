package com.seaboxdata.sdps.item.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.item.anotation.DataPermission;
import com.seaboxdata.sdps.item.dto.database.DatabaseDto;
import com.seaboxdata.sdps.item.dto.datasource.DataSourceDto;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.mapper.SdpsDatabaseMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceTypeMapper;
import com.seaboxdata.sdps.item.model.SdpsDatabase;
import com.seaboxdata.sdps.item.model.SdpsDatasource;
import com.seaboxdata.sdps.item.model.SdpsDatasourceType;
import com.seaboxdata.sdps.item.service.DataBaseOperator;
import com.seaboxdata.sdps.item.service.IDatabaseService;
import com.seaboxdata.sdps.item.service.IItemService;
import com.seaboxdata.sdps.item.vo.database.DatabaseRequest;
import com.seaboxdata.sdps.item.vo.datasource.DataSourceRequest;

@Service
public class DatabaseServiceImpl extends
		SuperServiceImpl<SdpsDatabaseMapper, SdpsDatabase> implements
		IDatabaseService {
	@Autowired
	private final Map<String, DataBaseOperator> dataBaseOperatorMap = new ConcurrentHashMap<>();
	@Autowired
	private SdpsDatasourceMapper datasourceMapper;

	@Autowired
	private SdpsDatasourceTypeMapper datasourceTypeMapper;

	@Autowired
	private IItemService itemService;

	@Override
	public void saveAndCreateDatabase(Long userId, SdpsDatabase database) {
		List<Long> hasItemIds = itemService.selectItemByUser(userId, true)
				.stream().map(ItemDto::getId).collect(Collectors.toList());
		if (!hasItemIds.contains(database.getItemId())) {
			throw new BusinessException("您无权为该项目建库");
		}
		DataSourceRequest request = new DataSourceRequest();
		request.setClusterId(database.getClusterId());
		request.setTypeId(database.getTypeId());
		List<DataSourceDto> dataSourceDtos = datasourceMapper.selectDatasource(
				request).getResult();
		if (CollUtil.isEmpty(dataSourceDtos)
				|| dataSourceDtos.stream().filter(DataSourceDto::getIsValid)
						.count() == 0) {
			throw new BusinessException("所选的数据源是无效的");
		}
		DataBaseOperator dataBaseOperator = dataBaseOperatorMap
				.get(dataSourceDtos.get(0).getType().toLowerCase());
		if (Objects.isNull(dataBaseOperator)) {
			throw new BusinessException("目前不支持该数据库类型");
		}
		boolean isExisit = dataBaseOperator.isExisitDataBase(dataSourceDtos
				.get(0).getName(), database.getName());
		if (isExisit)
			throw new BusinessException("所建数据库已存在");
		dataBaseOperator.createDataBase(dataSourceDtos.get(0).getName(),
				database.getName(), database.getDesc());
		this.save(database);
	}

	@Override
	public void deleteDatabaseByIds(Long userId, DatabaseRequest request) {
		List<SdpsDatabase> databases = this
				.list(new QueryWrapper<SdpsDatabase>().in("id",
						request.getIds()));
		if (CollUtil.isEmpty(databases)) {
			throw new BusinessException("所删除的数据不存在");
		}
		if (databases.stream().map(SdpsDatabase::getTypeId).distinct().count() != 1) {
			throw new BusinessException("删除的数据库不是同一个类型的");
		}
		if (databases.stream().map(SdpsDatabase::getClusterId).distinct()
				.count() != 1) {
			throw new BusinessException("删除的数据库不是同一个数据源的");
		}
		List<Long> allItemIds = databases.stream().map(SdpsDatabase::getItemId)
				.collect(Collectors.toList());
		List<Long> hasItemIds = itemService.selectItemByUser(userId, true)
				.stream().map(ItemDto::getId).collect(Collectors.toList());
		if (allItemIds.stream().filter(a -> {
			return !hasItemIds.contains(a);
		}).count() != 0) {
			throw new BusinessException("您无权删除该数据库");
		}
		SdpsDatasourceType datasourceType = datasourceTypeMapper
				.selectById(databases.get(0).getTypeId());
		SdpsDatasource datasource = datasourceMapper
				.selectOne(new QueryWrapper<SdpsDatasource>().eq("cluster_id",
						databases.get(0).getClusterId()).eq("type_id",
						databases.get(0).getTypeId()).eq("is_valid", 1));
		if(Objects.isNull(datasource)){
			throw new BusinessException("所选数据源是无效的");
		}
		DataBaseOperator dataBaseOperator = dataBaseOperatorMap
				.get(datasourceType.getName().toLowerCase());
		databases.forEach(database -> {
			List<String> tables = dataBaseOperator.selectTables(
					datasource.getName(), database.getName());
			if (CollUtil.isNotEmpty(tables)) {
				throw new BusinessException("数据库:" + database.getName()
						+ "不为空,请先删除库中的表:" + tables);
			}
		});
		databases.forEach(database -> {
			dataBaseOperator.dropDatabase(datasource.getName(),
					database.getName());
		});
		this.removeByIds(request.getIds());
	}

	@Override
	@DataPermission(joinName = "si.id")
	public PageResult<DatabaseDto> findDatabases(Integer page, Integer size,
			DatabaseRequest param) {
		PageHelper.startPage(page, size);
		Page<DatabaseDto> databaseDtos = this.baseMapper
				.findDatabasesByExample(param);
		return PageResult.<DatabaseDto> builder().code(0)
				.data(databaseDtos.getResult()).msg("操作成功")
				.count(databaseDtos.getTotal()).build();
	}

	@Override
	@DataPermission(joinName = "sdb.item_id")
	public List<DatabaseDto> selectDatabase(DatabaseRequest request) {
		Page<DatabaseDto> databaseDtos = this.baseMapper
				.findDatabasesByExample(request);
		return databaseDtos;
	}

	@Override
	public Map<String, String> getItemInfoByDatabaseName(Set<String> nameSet) {
		List<Map<String, String>> itemInfos = this.baseMapper
				.getItemInfoByDatabaseName(nameSet);
		Map<String, String> result = MapUtil.newHashMap();
		if (CollUtil.isNotEmpty(itemInfos)) {
			itemInfos.forEach(info -> {
				result.put(info.get("name"), info.get("itemName"));
			});
		}
		return result;
	}

}