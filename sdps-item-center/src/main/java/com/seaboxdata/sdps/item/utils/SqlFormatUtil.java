package com.seaboxdata.sdps.item.utils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.seaboxdata.sdps.common.core.exception.BusinessException;


@Slf4j
public class SqlFormatUtil {

	public static String sqlFormat(String sql, String type) {
		try {
			SQLStatementParser parser = SQLParserUtils
					.createSQLStatementParser(sql, type);
			List<SQLStatement> sqlStatements = parser.parseStatementList();
			sqlStatements
					.forEach(stmt -> {
						if (stmt instanceof SQLCreateTableStatement) {
							SQLCreateTableStatement SQLCreateTableStatement = (SQLCreateTableStatement) stmt;
							for (SQLTableElement sqlStatement : SQLCreateTableStatement.getTableElementList()) {
								if(sqlStatement instanceof SQLColumnDefinition){
									SQLColumnDefinition SQLColumnDefinition =(SQLColumnDefinition)sqlStatement;
									if(StrUtil.contains(SQLColumnDefinition.getNameAsString(), "'")){
										throw new BusinessException("hive sql语法校验错误");
									}
								}
							}
						} else{
							throw new BusinessException("输入sql不是创建表语句");
						}
//						if (stmt instanceof SQLSelectStatement) {
//						} else if (stmt instanceof SQLInsertStatement) {
//						} else if (stmt instanceof SQLUpdateStatement) {
//						} else if (stmt instanceof SQLDeleteStatement) {
//						} else if (stmt instanceof SQLAlterTableStatement) {
//						} else if (stmt instanceof SQLExplainStatement) {
//						} else if (stmt instanceof SQLDropTableStatement) {
//						} else {
//
//						}

					});
		} catch (ParserException e) {
			log.error("SQL转换中发生了错误：" + e.getMessage());
			throw e;
		}
		return SQLUtils.format(sql, type);
	}

	public static void main(String[] args) {
		String sql = "CREATE TABLE test4('id' int);";
		System.out.println(sqlFormat(sql, "hive"));
	}

}
