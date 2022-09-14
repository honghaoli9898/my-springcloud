package com.seaboxdata.sdps.uaa.oauth.service;

import java.util.Map;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.uaa.oauth.model.TokenVo;

/**
 * @author zlt
 */
public interface ITokensService {
    /**
     * 查询token列表
     * @param params 请求参数
     * @param clientId 应用id
     */
    PageResult<TokenVo> listTokens(Map<String, Object> params, String clientId);
}
