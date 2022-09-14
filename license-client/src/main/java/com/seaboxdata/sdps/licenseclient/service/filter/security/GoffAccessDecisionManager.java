package com.seaboxdata.sdps.licenseclient.service.filter.security;

import com.seaboxdata.sdps.licenseclient.intf.filter.security.SecurityResourceIntf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Service;


import java.util.Collection;
import java.util.Iterator;

@Service
//如果用户实现了这个，我们默认用户需要启用这个过滤器进行权限拦截
@ConditionalOnClass({UserDetailsService.class, SecurityResourceIntf.class})
public class GoffAccessDecisionManager implements AccessDecisionManager{

    private static final Logger logger = LoggerFactory.getLogger(GoffAccessDecisionManager.class);

    // decide 方法是判定是否拥有权限的决策方法，
    // authentication 是释CustomUserService中循环添加到 GrantedAuthority
    // 对象中的权限信息集合.用户拥有的权限集合
    // object 包含客户端发起的请求的requset信息，可转换为 HttpServletRequest request =
    // ((FilterInvocation) object).getHttpRequest();
    // configAttributes 为MyInvocationSecurityMetadataSource的getAttributes(Object
    // object)这个方法返回的结果，此方法是为了判定用户请求的url 是否在权限表中，如果在权限表中，则返回给 decide
    // 方法，用来判定用户是否有此权限。如果不在权限表中则放行。
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {

        if (null == configAttributes || configAttributes.size() <= 0) {
            return;
        }
        ConfigAttribute c;
        FilterInvocation fi = (FilterInvocation) object;
        String reqUrl = fi.getRequestUrl();
        //如果是rest风格，可能请求的路径比资源配置时候的url长,因此不能直接使用相等判断
        //1. 先判断是否对请求的资源进行了权限限制，如果没有，就跳过访问控制
        boolean needauth = false;
        for (Iterator<ConfigAttribute> iter = configAttributes.iterator(); iter.hasNext(); ) {
            c = iter.next();
            if (reqUrl.indexOf(c.getAttribute()) >= 0) {
                needauth = true;
                break;
            }
        }
        if (!needauth) {
            return;
        }
        //系统对访问的资源进行了权限限制，那么判断这个用户是否有权限访问此资源
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (reqUrl.indexOf(auth.getAuthority()) >= 0) {
                return;
            }
        }
        String msg = authentication.getName() + "没有权限访问" + reqUrl;
        logger.error(msg);
        throw new AccessDeniedException(msg);

    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
