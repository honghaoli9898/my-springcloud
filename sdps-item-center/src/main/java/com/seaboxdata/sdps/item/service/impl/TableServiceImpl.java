package com.seaboxdata.sdps.item.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.item.anotation.DataPermission;
import com.seaboxdata.sdps.item.dto.table.TableDto;
import com.seaboxdata.sdps.item.mapper.SdpsDatabaseMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceMapper;
import com.seaboxdata.sdps.item.mapper.SdpsDatasourceTypeMapper;
import com.seaboxdata.sdps.item.mapper.SdpsTableMapper;
import com.seaboxdata.sdps.item.model.SdpsDatabase;
import com.seaboxdata.sdps.item.model.SdpsDatasource;
import com.seaboxdata.sdps.item.model.SdpsDatasourceType;
import com.seaboxdata.sdps.item.model.SdpsTable;
import com.seaboxdata.sdps.item.service.DataBaseOperator;
import com.seaboxdata.sdps.item.service.ITableService;
import com.seaboxdata.sdps.item.utils.DdlFormatUtil;
import com.seaboxdata.sdps.item.vo.table.TableRequest;

@Slf4j
@Service
public class TableServiceImpl extends
		SuperServiceImpl<SdpsTableMapper, SdpsTable> implements ITableService {
	@Autowired
	private SdpsDatasourceTypeMapper datasourceTypeMapper;
	@Autowired
	private SdpsDatabaseMapper databaseMapper;
	@Autowired
	private final Map<String, DataBaseOperator> dataBaseOperatorMap = new ConcurrentHashMap<String, DataBaseOperator>();
	@Autowired
	private DynamicSqlExecutor dynamicSqlExecutor;
	@Autowired
	private SdpsDatasourceMapper datasourceMapper;

	@Override
	public void saveAndCreateTable(SdpsTable table) {
		if (!table.getCreateMode()) {
			String regex = "create[\\s\\S]+table([\\S\\s]*)\\(";
			String sql = table.getFieldSql().toLowerCase();
			String enName = ReUtil.get(regex, sql, 1);
			if (StrUtil.isBlank(enName)) {
				throw new BusinessException("未匹配到表名");
			}
			if (enName.length() >= 50) {
				throw new BusinessException("表名过长");
			}
			regex = "create[\\s\\S]+\\)[\\s]*comment[\\s]+['|\"]?([\\s\\S]+?)['|\"'?]";
			String cnName = ReUtil.get(regex, sql, 1);
			if (StrUtil.isBlank(cnName)) {
				cnName = "";
			} else {
				cnName = cnName.trim();
			}
			table.setCnName(cnName);
			table.setEnName(enName.trim());
		}
		SdpsDatasourceType datasourceType = datasourceTypeMapper
				.selectById(table.getTypeId());
		DataBaseOperator dataBaseOperator = dataBaseOperatorMap
				.get(datasourceType.getName().toLowerCase());
		if (Objects.isNull(dataBaseOperator)) {
			throw new BusinessException("暂不支持该类型的数据表创建");
		}
		SdpsDatabase database = databaseMapper
				.selectById(table.getDatabaseId());
		SdpsDatasource datasource = datasourceMapper
				.selectOne(new QueryWrapper<SdpsDatasource>()
						.eq("cluster_id", database.getClusterId())
						.eq("type_id", database.getTypeId()).eq("is_valid", 1));
		if (Objects.isNull(datasource)) {
			throw new BusinessException("所选数据源是无效的");
		}
		List<String> tables = dataBaseOperator.selectTables(
				datasource.getName(), database.getName());
		if (CollUtil.isNotEmpty(tables) && tables.contains(table.getEnName())) {
			throw new BusinessException("表已存在");
		}
		String ddl = DdlFormatUtil.buildDdlFieldScript(
				datasourceType.getName(), table);
		ddl = "use " + database.getName() + ";\n" + ddl;
		log.info("执行sql:{}", ddl);
		dynamicSqlExecutor.updateSql(datasource.getName(), ddl);
		try {
			save(table);
		} catch (Exception e) {
			ddl = "use " + database.getName() + ";\n" + "drop table "
					+ table.getEnName() + ";";
			log.info("执行sql:{}", ddl);
			dynamicSqlExecutor.updateSql(datasource.getName(), ddl);
			throw e;
		}

	}

	@Override
	@DataPermission(joinName = "st.item_id")
	public PageResult<TableDto> findTables(Integer page, Integer size,
			TableRequest param) {
		PageHelper.startPage(page, size);
		Page<TableDto> tableDtos = this.baseMapper.findTablesByExample(param);
		return PageResult.<TableDto> builder().code(0)
				.data(tableDtos.getResult()).msg("操作成功")
				.count(tableDtos.getTotal()).build();
	}

	@Override
	public void deleteTableByIds(TableRequest request) {
		List<SdpsTable> tables = this.listByIds(request.getIds());
		long count = tables.stream().map(SdpsTable::getTypeId).distinct()
				.count();
		if (count != 1) {
			throw new BusinessException("删除的数据表不是同一个数据源的");
		}
		SdpsDatasourceType datasourceType = datasourceTypeMapper
				.selectById(tables.get(0).getTypeId());
		SdpsDatabase database = databaseMapper.selectById(tables.get(0)
				.getDatabaseId());
		SdpsDatasource datasource = datasourceMapper
				.selectOne(new QueryWrapper<SdpsDatasource>()
						.eq("cluster_id", database.getClusterId())
						.eq("type_id", database.getTypeId()).eq("is_valid", 1));
		if (Objects.isNull(datasource)) {
			throw new BusinessException("所选数据源是无效的");
		}
		DataBaseOperator dataBaseOperator = dataBaseOperatorMap
				.get(datasourceType.getName().toLowerCase());
		tables.forEach(table -> {
			dataBaseOperator.dropTable(datasource.getName(),
					database.getName(), table.getEnName());
		});
		this.removeByIds(request.getIds());
	}

}