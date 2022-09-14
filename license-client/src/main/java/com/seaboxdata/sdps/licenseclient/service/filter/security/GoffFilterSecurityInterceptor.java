package com.seaboxdata.sdps.licenseclient.service.filter.security;

import com.seaboxdata.sdps.licenseclient.intf.filter.security.SecurityResourceIntf;
import com.seaboxdata.sdps.licenseclient.service.filter.security.GoffAccessDecisionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Service;


import javax.servlet.*;
import java.io.IOException;

@Service
//如果用户实现了这个，我们默认用户需要启用这个过滤器进行权限拦截
@ConditionalOnClass({UserDetailsService.class, SecurityResourceIntf.class})
public class GoffFilterSecurityInterceptor extends AbstractSecurityInterceptor implements Filter{

    @Autowired
    private FilterInvocationSecurityMetadataSource securityMetadataSource;

    @Autowired
    public void setGoffAccessDecisionManager(GoffAccessDecisionManager goffAccessDecisionManager) {
        super.setAccessDecisionManager(goffAccessDecisionManager);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        FilterInvocation fi = new FilterInvocation(request, response, chain);
        // beforeInvocation会获取受保护的资源 ，并且如果需要鉴权，将调用AccessDecisionManager.decide判断
        // 校验用户的权限是否足够
        InterceptorStatusToken token = super.beforeInvocation(fi);
        try {
            // 执行下一个拦截器
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        } finally {
            super.afterInvocation(token, null);
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return this.securityMetadataSource;
    }
}
