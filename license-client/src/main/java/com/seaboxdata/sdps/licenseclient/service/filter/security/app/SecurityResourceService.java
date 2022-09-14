package com.seaboxdata.sdps.licenseclient.service.filter.security.app;

import com.seaboxdata.sdps.licenseclient.intf.filter.security.SecurityResourceIntf;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Collection;

//这里加载所有的受保护的资源，作为demo这里硬编码实现，实际情况应该根据需要修改，从数据库或其他加载应用所有的受保护的资源
@Service
public class SecurityResourceService implements SecurityResourceIntf{

    /**
     * 加载权限表中所有权限
     */
    @Override
    public Collection<ConfigAttribute> loadResource() {
        Collection<ConfigAttribute> array = new ArrayList<>();
        array.add(new SecurityConfig("/test/redis"));
        //array.add(new SecurityConfig("/test/hystrix"));
        //array.add(new SecurityConfig("/unixtime"));
        return array;
    }
}
