package com.seaboxdata.sdps.licenseclient.service.filter.security;

import com.seaboxdata.sdps.licenseclient.intf.filter.security.SecurityResourceIntf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;


@Configuration
@EnableWebSecurity
//如果用户实现了这个，我们默认用户需要启用这个过滤器进行权限拦截
@ConditionalOnClass({UserDetailsService.class, SecurityResourceIntf.class})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Autowired
    private GoffFilterSecurityInterceptor goffFilterSecurityInterceptor;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsService customUserService = getApplicationContext().getBean(UserDetailsService.class);
        auth.userDetailsService(customUserService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().//anyRequest().authenticated() // 任何请求,登录后可以访问
                //.and().formLogin().loginPage("/login")
//                .failureUrl("/login?error")
                //.permitAll() // 登录页面用户任意访问
                        and().logout().permitAll(); // 注销行为任意访问
        http.csrf().disable();
        http.addFilterBefore(goffFilterSecurityInterceptor, FilterSecurityInterceptor.class);
    }
}

