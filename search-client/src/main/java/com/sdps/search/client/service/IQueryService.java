package com.sdps.search.client.service;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sdps.search.model.LogicDelDto;
import com.sdps.search.model.SearchDto;
import com.seaboxdata.sdps.common.core.model.PageResult;

/**
 * 搜索客户端接口
 *
 * @author zlt
 * @date 2019/4/24
 */
public interface IQueryService {
    /**
     * 查询文档列表
     * @param indexName 索引名
     * @param searchDto 搜索Dto
     */
    PageResult<JsonNode> strQuery(String indexName, SearchDto searchDto);

    /**
     * 查询文档列表
     * @param indexName 索引名
     * @param searchDto 搜索Dto
     * @param logicDelDto 逻辑删除Dto
     */
    PageResult<JsonNode> strQuery(String indexName, SearchDto searchDto, LogicDelDto logicDelDto);

    /**
     * 访问统计聚合查询
     * @param indexName 索引名
     * @param routing es的路由
     */
    Map<String, Object> requestStatAgg(String indexName, String routing);
}
