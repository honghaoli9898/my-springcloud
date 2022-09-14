package com.seaboxdata.sdps.licenseclient.service.filter.security.app;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//用户username（用户账号）可以访问的资源
//TODO: app需要根据情况修改实现，这个是作为演示的硬编码方式实现，根据实际情况修改，或者屏蔽这个bean,如果app不需要做web鉴权
@Service
public class CustomUserService implements UserDetailsService{

    //应用需要实现根据自己的登录时候的username（一般来说是账号）获取这个账号可以访问的权限，
    //作为demo我们这里采用的是硬编码方式,应用根据自己的情况，可以从数据库或其他获取用户对应的权限
    //我们这里采取url匹配方式，用户username可以访问什么url资源
    @Override
    public UserDetails loadUserByUsername(String username) {
        //1. load user info by username，如果加载不成功，抛出UsernameNotFoundException
        //2. load user Permission by user info
        //3. add user Permission and return
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (true) {
            //这里，username这个用户可以访问2个服务
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("/test/hystrix");
            grantedAuthorities.add(grantedAuthority);
            grantedAuthority = new SimpleGrantedAuthority("/test/redis");
            grantedAuthorities.add(grantedAuthority);
            //和TestController.login，假如数据库中12345abC对应的在数据库里的密码是 DK==DKAKKSKKKSHNC=72718134CDWDDDDLKAJA,passwd参数里值应该是
            //DK==DKAKKSKKKSHNC=72718134CDWDDDDLKAJA
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String passwd = bCryptPasswordEncoder.encode("12345abC");
            return new User(username, passwd, grantedAuthorities);
        } else {
            throw new UsernameNotFoundException("账号: " + username + "不存在");
        }
    }


}
