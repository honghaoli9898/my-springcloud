package com.seaboxdata.sdps.licenseclient.service.filter.security;

import com.seaboxdata.sdps.licenseclient.intf.filter.security.SecurityResourceIntf;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Service;


import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Service
//如果用户实现了这个，我们默认用户需要启用这个过滤器进行权限拦截
@ConditionalOnClass({UserDetailsService.class, SecurityResourceIntf.class})
public class GoffInvocationSecurityMetadataSourceService implements FilterInvocationSecurityMetadataSource,
        ApplicationContextAware{

    private Collection<ConfigAttribute> resource = null;

    private ApplicationContext applicationContext;

    // 此方法是为了判定用户请求的url 是否在权限表中，如果在权限表中，则返回给 decide 方法，用来判定用户是否有此权限。如果不在权限表中则放行。
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if (resource == null) {
            resource = applicationContext.getBean(SecurityResourceIntf.class).loadResource();
        }
        HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();
        AntPathRequestMatcher matcher;
        for (ConfigAttribute ca : resource) {
            matcher = new AntPathRequestMatcher(ca.getAttribute());
            if (matcher.matches(request)) {
                return resource;
            }
        }
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return resource;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
