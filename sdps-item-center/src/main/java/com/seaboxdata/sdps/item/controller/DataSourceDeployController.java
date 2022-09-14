package com.seaboxdata.sdps.item.controller;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;

import org.apache.hive.com.esotericsoftware.minlog.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.item.dto.datasource.DataSourceDto;
import com.seaboxdata.sdps.item.model.SdpsDatasource;
import com.seaboxdata.sdps.item.model.SdpsDatasourceParam;
import com.seaboxdata.sdps.item.model.SdpsDatasourceType;
import com.seaboxdata.sdps.item.resolver.impl.DataSourceResolverComposite;
import com.seaboxdata.sdps.item.service.IDatasourceService;
import com.seaboxdata.sdps.item.service.impl.DynamicSqlExecutor;
import com.seaboxdata.sdps.item.utils.DbUtil;
import com.seaboxdata.sdps.item.vo.datasource.DataSourceRequest;

@RestController
@RequestMapping("/MC30")
public class DataSourceDeployController {
	@Autowired
	private IDatasourceService datasourceService;
	@Autowired
	private DataSourceResolverComposite dataSourceResolverComposite;
	@Autowired
	private DynamicSqlExecutor dynamicSqlExecutor;
	@Autowired
	private DataSource dataSource;

	/**
	 * 查询数据源extend、cluster类型
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/MC3001")
	public Result<List<SdpsDatasourceType>> findDataSourceType(
			@RequestBody DataSourceRequest request) {
		List<SdpsDatasourceType> results = datasourceService
				.findDataSourceType(request);
		return Result.succeed(results, "操作成功");
	}

	/**
	 * 保存数据源
	 * 
	 * @param sysUser
	 * @param datasource
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/MC3002")
	public Result saveDataSource(@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody SdpsDatasource datasource) throws Exception {
		if (CollUtil.isEmpty(datasource.getItemIds())
				|| StrUtil.isBlank(datasource.getProperties())
				|| Objects.isNull(datasource.getTypeId())
				|| StrUtil.isBlank(datasource.getName())) {
			return Result.failed("关联项目、数据源名称、数据源配置、数据源类型不能为空");
		}
		if (!JSONUtil.isJson(datasource.getProperties())) {
			return Result.failed("数据源配置必须为json格式,请检查");
		}
		if (datasourceService.count(new QueryWrapper<SdpsDatasource>().eq(
				"name", datasource.getName())) > 0) {
			return Result.failed("已存在同名的数据源");
		}
		datasource.setSystemInit(false);
		datasource.setIsValid(true);
		datasource.setIsVisible(true);
		datasource.setCreateUser(sysUser.getUsername());
		datasource.setCreateUserId(sysUser.getId());
		datasource.setUpdateUser(sysUser.getUsername());
		datasource.setUpdateUserId(sysUser.getId());
		datasourceService.saveDataSourceType(datasource);
		return Result.succeed("操作成功");
	}

	@PostMapping("/MC3003")
	public PageResult findDatasourcePage(@LoginUser SysUser sysUser,
			@Validated @RequestBody PageRequest<DataSourceRequest> request) {
		if(Objects.isNull(request.getParam())){
			request.setParam(new DataSourceRequest());
		}
		request.getParam().setUsername(sysUser.getUsername());
		request.getParam().setUserId(sysUser.getId());
		PageResult<DataSourceDto> results = datasourceService
				.findDatasourcePage(request);
		return results;
	}

	@PostMapping("/MC3004")
	public Result findDatasourceParamByType(
			@RequestBody DataSourceRequest request) {
		if (Objects.isNull(request.getId())) {
			return Result.failed("数据源类型id不能为空");
		}
		List<SdpsDatasourceParam> results = datasourceService
				.findDatasourceParamByType(request);
		return Result.succeed(results, "操作成功");
	}

	@PostMapping("/MC3005")
	public Result updateDatasourceById(
			@LoginUser(isFull = false) SysUser sysUser,
			@RequestBody SdpsDatasource datasource) throws Exception {
		if (Objects.isNull(datasource.getId())) {
			return Result.failed("数据源id不能为空");
		}
		if (StrUtil.isNotBlank(datasource.getName())
				&& datasourceService.count(new QueryWrapper<SdpsDatasource>()
						.eq("name", datasource.getName()).ne("id",datasource.getId() )) > 0) {
			return Result.failed("更新的名字在数据库存在");
		}
		datasource.setUpdateUser(sysUser.getUsername());
		datasource.setUpdateUserId(sysUser.getId());
		datasourceService.updateDatasourceById(datasource);
		return Result.succeed("操作成功");
	}

	@PostMapping("/MC3006")
	public Result deleteDatasourceByIds(@RequestBody DataSourceRequest request) {
		if (CollUtil.isEmpty(request.getIds())) {
			return Result.succeed("操作成功");
		}
		datasourceService.deleteDatasourceByIds(request);
		return Result.succeed("操作成功");
	}

	@PostMapping("/MC3007")
	public Result testDatasource(@RequestBody DataSourceRequest request)
			throws Exception {
		if (StrUtil.isBlank(request.getName())
				|| StrUtil.isBlank(request.getProperties())) {
			return Result.failed("数据源类型、数据源配置不能为空");
		}
		if (!JSONUtil.isJson(request.getProperties())) {
			return Result.failed("数据源配置必须为json格式");
		}
		DataSourceProperty dataSourceProperty = dataSourceResolverComposite
				.resolveArgument(request.getName(), request.getProperties());
		return Result.succeed(DbUtil.testDatasource(
				dataSourceProperty.getDriverClassName(),
				dataSourceProperty.getUrl(), dataSourceProperty.getUsername(),
				dataSourceProperty.getPassword()), "操作成功");
	}

	@PostMapping("/MC3008")
	public Result executorSql(@RequestBody DataSourceRequest request)
			throws Exception {
		if (StrUtil.isBlank(request.getName())
				|| StrUtil.isBlank(request.getSql())) {
			return Result.failed("数据源类型、sql语句不能为空");
		}
		List<String> sqlList = StrUtil.splitTrim(request.getSql(), ';');
		if (StrUtil.containsAnyIgnoreCase(sqlList.get(sqlList.size() - 1),
				"create", "alert")) {
			return Result.succeed(
					dynamicSqlExecutor.executeSql(request.getName(),
							request.getSql()), "操作成功");
		}
		if (StrUtil.containsAnyIgnoreCase(sqlList.get(sqlList.size() - 1),
				"insert", "update")) {
			return Result.succeed(
					dynamicSqlExecutor.updateSql(request.getName(),
							request.getSql()), "操作成功");
		}
		return Result.succeed(dynamicSqlExecutor.querySql(request.getName(),
				request.getSql()), "操作成功");

	}

	@GetMapping("/MC3009")
	public Set<String> now() {
		DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
		return ds.getCurrentDataSources().keySet();
	}

	@PostMapping("/MC3010")
	public Result<List<DataSourceDto>> findDatasource(
			@RequestBody DataSourceRequest request) {
		Result<List<DataSourceDto>> results = datasourceService
				.findDatasource(request);
		return results;
	}

	@PostMapping("/MC3011")
	public Result<List<SdpsDatasourceType>> selectDataSourceType(
			@RequestBody DataSourceRequest request) {
		Result<List<SdpsDatasourceType>> results = datasourceService
				.selectDataSourceType(request);
		return results;
	}

	/**
	 * 根据集群id获取hive数据源开启kerberos时的principal
	 * @param clusterId 集群id
	 * @return
	 */
	@GetMapping("/getHivePrincipal")
	public String getHivePrincipal(@RequestParam("clusterId") Integer clusterId) {
		return datasourceService.getHivePrincipal(clusterId);
	}

	/**
	 * 更新keytab文件
	 *
	 * @param list
	 * @return
	 */
	@PostMapping("/updateKeytab")
	public Result updateKeytab(@RequestBody List<SdpServerKeytab> list) {
		try {
			datasourceService.updateKeytabs(list);
			return Result.succeed("成功");
		} catch (Exception e) {
			Log.error("更新keytab报错",e);
			return Result.failed(e.getMessage());
		}
	}
}
