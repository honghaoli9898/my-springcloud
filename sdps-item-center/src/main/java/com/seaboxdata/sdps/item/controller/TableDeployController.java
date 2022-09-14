package com.seaboxdata.sdps.item.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.item.dto.table.TableDto;
import com.seaboxdata.sdps.item.mapper.SdpsTableMapper;
import com.seaboxdata.sdps.item.model.SdpsTable;
import com.seaboxdata.sdps.item.service.ITableService;
import com.seaboxdata.sdps.item.vo.table.TableRequest;

@RestController
@RequestMapping("/MC40")
public class TableDeployController {
    @Autowired
    private ITableService tableService;
    @Autowired
    private SdpsTableMapper sdpsTableMapper;

    @PostMapping("/MC4001")
    public Result saveAndCreateTable(@RequestBody SdpsTable table) {
        if (table.getCreateMode()) {
            if (StrUtil.isBlank(table.getEnName())) {
                Result.failed("表名不能为空");
            }
            if(table.getEnName().length() >=50){
            	Result.failed("表名过长");
            }
        }
        if (StrUtil.isBlank(table.getFieldSql())
                || Objects.isNull(table.getTypeId())
                || Objects.isNull(table.getClusterId())
                || Objects.isNull(table.getDatabaseId())
                || Objects.isNull(table.getItemId())) {
            return Result.failed("表类型、关联集群、关联数据库、关联项目、数据表字段不能为空");
        }
        if (table.getCreateMode() && !JSONUtil.isJson(table.getFieldSql())) {
            return Result.failed("数据表字段必须为json格式");
        }
        if (StrUtil.isNotBlank(table.getSenior())
                && (!JSONUtil.isJson(table.getSenior()))) {
            return Result.failed("高级选项必须为json格式");
        }
        tableService.saveAndCreateTable(table);
        return Result.succeed("操作成功");
    }

    @PostMapping("/MC4002")
    public PageResult<TableDto> selectTablePage(
            @RequestBody PageRequest<TableRequest> request) {
    	if(Objects.isNull(request.getParam())){
    		request.setParam(new TableRequest());
    	}
        return tableService.findTables(request.getPage(),request.getSize(),request.getParam());
    }

    @GetMapping("/selectTableList")
    public String selectTableList() {
    	PageRequest<TableRequest> tableRequest = new PageRequest<TableRequest>();
        tableRequest.setPage(0);
        tableRequest.setSize(Integer.MAX_VALUE);
        Page<TableDto> tableDtos = sdpsTableMapper.findTablesByExample(tableRequest.getParam());
        return JSON.toJSONString(tableDtos);
    }


    @PostMapping("/MC4003")
    public Result deleteTableByIds(@RequestBody TableRequest request) {
        if (CollUtil.isEmpty(request.getIds())) {
            return Result.succeed("操作成功");
        }
        tableService.deleteTableByIds(request);
        return Result.succeed("操作成功");
    }
}
