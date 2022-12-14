package com.seaboxdata.sdps.provider.gateway.auth;

import java.util.List;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import com.seaboxdata.sdps.common.core.model.SysMenu;
import com.seaboxdata.sdps.common.oauth2.service.impl.DefaultPermissionServiceImpl;
import com.seaboxdata.sdps.provider.gateway.feign.MenuService;

/**
 * url权限认证
 */
@Slf4j
@Component
public class PermissionAuthManager extends DefaultPermissionServiceImpl implements ReactiveAuthorizationManager<AuthorizationContext> {
    @Resource
    private MenuService menuService;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext authorizationContext) {
        return authentication.map(auth -> {
            ServerWebExchange exchange = authorizationContext.getExchange();
            ServerHttpRequest request = exchange.getRequest();
            boolean isPermission = super.hasPermission(auth, request.getMethodValue(), request.getURI().getPath());
            return new AuthorizationDecision(isPermission);
        }).defaultIfEmpty(new AuthorizationDecision(false));
    }

    @Override
    public List<SysMenu> findMenuByRoleCodes(String roleCodes) {
        return menuService.findByRoleCodes(roleCodes);
    }
}
