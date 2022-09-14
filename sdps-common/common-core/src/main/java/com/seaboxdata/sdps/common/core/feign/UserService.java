package com.seaboxdata.sdps.common.core.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.seaboxdata.sdps.common.core.config.MultipartSupportConfig;
import com.seaboxdata.sdps.common.core.constant.ServiceNameConstants;
import com.seaboxdata.sdps.common.core.feign.fallback.UserServiceFallbackFactory;
import com.seaboxdata.sdps.common.core.model.LoginAppUser;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;

@FeignClient(name = ServiceNameConstants.USER_SERVICE, fallbackFactory = UserServiceFallbackFactory.class, decode404 = true,configuration = MultipartSupportConfig.class)
public interface UserService {
    /**
     * feign rpc访问远程/users/{username}接口
     * 查询用户实体对象SysUser
     *
     * @param username
     * @return
     */
    @GetMapping(value = "/users/name/{username}")
    SysUser selectByUsername(@PathVariable("username") String username);

    /**
     * feign rpc访问远程接口
     *
     * @param username
     * @return
     */
    @GetMapping(value = "/users-anon/login", params = "username")
    LoginAppUser findByUsername(@RequestParam("username") String username);

    /**
     * 通过手机号查询用户、角色信息
     *
     * @param mobile 手机号
     */
    @GetMapping(value = "/users-anon/mobile", params = "mobile")
    LoginAppUser findByMobile(@RequestParam("mobile") String mobile);

    /**
     * 根据OpenId查询用户信息
     *
     * @param openId openId
     */
    @GetMapping(value = "/users-anon/openId", params = "openId")
    LoginAppUser findByOpenId(@RequestParam("openId") String openId);
    
	/**
	 * 组件登录
	 * 
	 * @return
	 */
	@GetMapping(value = "/server/login")
	public Result<Map<String, String>> serverLogin(
			@RequestParam("clusterId") String clusterId,
			@RequestParam("type") String type,
			@RequestParam("username") String username,
			@RequestParam(value = "isCache") Boolean isCache);


    /**
     * 通过用户id获取用户信息
     * @param userId
     * @return
     */
    @GetMapping(value = "/users/name", params = "userId")
    public SysUser selectByUserId(@RequestParam("userId") String userId);
}
