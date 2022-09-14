package com.seaboxdata.sdps.licenseclient.intf.filter.security;

import org.springframework.security.access.ConfigAttribute;

import java.util.Collection;

/**
 * 应用里所有需要进行访问保护的资源
 *
 * @author stonehan
 */
public interface SecurityResourceIntf{

    Collection<ConfigAttribute> loadResource();
}
