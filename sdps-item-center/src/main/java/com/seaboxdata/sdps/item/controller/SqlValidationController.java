package com.seaboxdata.sdps.item.controller;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.core.api.BaseController;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.item.utils.SqlFormatUtil;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2020-07-07
 * @time 22:00
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class SqlValidationController extends BaseController {

    @RequestMapping("/checkSql")
    public Result checkSql(@RequestParam("sql")String sql,@RequestParam("type")String type) {
        if (StringUtils.isEmpty(sql)) {
            return Result.failed("sql 参数不能为空");
        }

        try {
            String listSql = SqlFormatUtil.sqlFormat(sql,type.toLowerCase());
            if (StrUtil.isBlank(listSql)) {
                return Result.failed("没有检测到有效sql语句,是否缺少了 ; 分隔符");
            }
        } catch (Exception e) {
            log.warn("校验失败Sql={},errorMessage= {} ", sql, e.getMessage());
            return Result.failed(e.getMessage());
        }

        return Result.succeed("操作成功");
    }

    @RequestMapping("/formatSql")
    public Result formatSql(@RequestParam("sql")String sql,@RequestParam("type")String type) {
        if (StringUtils.isEmpty(sql)) {
            return Result.failed("sql 参数不能为空");
        }
        String resultSql = null;
        try {
        	resultSql = SqlFormatUtil.sqlFormat(sql,type.toLowerCase());
            if (StrUtil.isBlank(resultSql)) {
                return Result.failed("没有检测到有效sql语句,是否缺少了 ; 分隔符");
            }
        } catch (Exception e) {
            log.warn("校验失败Sql={},errorMessage= {} ", sql, e.getMessage());
            return Result.failed(e.getMessage());
        }

        return Result.succeed(resultSql,"操作成功");
    }
}
