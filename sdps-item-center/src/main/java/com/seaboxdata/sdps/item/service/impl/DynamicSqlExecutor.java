package com.seaboxdata.sdps.item.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class DynamicSqlExecutor {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@DS("#type")
	public List<Map<String, Object>> querySql(String type, String sql) {
		List<String> sqlList = StrUtil.splitTrim(sql, ';');
		for (String subSql : CollUtil.sub(sqlList, 0, sqlList.size() - 1)) {
			jdbcTemplate.execute(subSql);
		}
		return jdbcTemplate.queryForList(sqlList.get(sqlList.size() - 1));
	}

	@DS("#type")
	public int updateSql(String type, String sql) {
		List<String> sqlList = StrUtil.splitTrim(sql, ';');
		for (String subSql : CollUtil.sub(sqlList, 0, sqlList.size() - 1)) {
			jdbcTemplate.execute(subSql);
		}
		return jdbcTemplate.update(sqlList.get(sqlList.size() - 1));
	}

	@DS("#type")
	public boolean executeSql(String type, String sql) {
		List<String> sqlList = StrUtil.splitTrim(sql, ';');
		for (String subSql : sqlList) {
			jdbcTemplate.execute(subSql);
		}
		return true;
	}

}
