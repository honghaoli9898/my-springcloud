package com.sdps.search.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sdps.search.model.SearchDto;
import com.seaboxdata.sdps.common.core.model.PageResult;

/**
 * @author zlt
 * @date 2019/4/24
 */
public interface ISearchService {
    /**
     * StringQuery通用搜索
     * @param indexName 索引名
     * @param searchDto 搜索Dto
     * @return
     */
    PageResult<JsonNode> strQuery(String indexName, SearchDto searchDto) throws IOException;
}
