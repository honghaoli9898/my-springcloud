package com.seaboxdata.sdps.common.core.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.constant.SQLConstants;

import java.util.List;
import java.util.Map;

/**
 * SQL工具类
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/10
 */
public class SqlTool {

    /**
     * SQL参数构建
     *
     * @param queryWrapper queryWrapper
     * @param params       参数 值 类型
     */
    public static void paramsTypeBuild(QueryWrapper<?> queryWrapper, List<JSONObject> params) {
        params.forEach(param -> {
            String name = MetaTool.fieldToColumn(param.getString("name"));
            Object value = param.get("value");
            switch (param.getString("type").toLowerCase()) {
                case SQLConstants.LE:
                    queryWrapper.le(name, value);
                    break;
                case SQLConstants.GE:
                    queryWrapper.ge(name, value);
                    break;
                case SQLConstants.LT:
                    queryWrapper.lt(name, value);
                    break;
                case SQLConstants.GT:
                    queryWrapper.gt(name, value);
                    break;
                case SQLConstants.LK:
                    queryWrapper.like(name, value);
                    break;
                default:
                    queryWrapper.eq(name, value);
                    break;
            }
        });
    }

    /**
     * queryWrapper 设置参数
     *
     * @param queryWrapper queryWrapper
     * @param column       字段
     * @param value        字段值
     * @param type         SQL匹配类型
     */
    public static void putParam(QueryWrapper<?> queryWrapper, String column, Object value, String type) {
        if (value != null) {
            switch (type.toLowerCase()) {
                case SQLConstants.LE:
                    queryWrapper.le(column, value);
                    break;
                case SQLConstants.GE:
                    queryWrapper.ge(column, value);
                    break;
                case SQLConstants.LT:
                    queryWrapper.lt(column, value);
                    break;
                case SQLConstants.GT:
                    queryWrapper.gt(column, value);
                    break;
                case SQLConstants.LK:
                    queryWrapper.like(column, value);
                    break;
                default:
                    queryWrapper.eq(column, value);
                    break;
            }
        }
    }

    /**
     * 设置 相等 的参数
     *
     * @param queryWrapper queryWrapper
     * @param column       字段
     * @param value        字段值
     */
    public static void setEqParam(QueryWrapper<?> queryWrapper, String column, Object value) {
        putParam(queryWrapper, column, value, SQLConstants.EQ);
    }

    public static void setEqParamList(QueryWrapper<?> queryWrapper, List<Map<String, Object>> paramValues) {
        paramValues.forEach(map -> putParam(queryWrapper, String.valueOf(paramValues), map.get("value"), "EQ"));
    }

    public static void setGeParam(QueryWrapper<?> queryWrapper, String column, Object value) {
        putParam(queryWrapper, column, value, SQLConstants.GE);
    }

    public static void setLeParam(QueryWrapper<?> queryWrapper, String column, Object value) {
        putParam(queryWrapper, column, value, SQLConstants.LE);
    }

    public static void setLkParam(QueryWrapper<?> queryWrapper, String column, Object value) {
        putParam(queryWrapper, column, value, SQLConstants.LK);
    }
}
