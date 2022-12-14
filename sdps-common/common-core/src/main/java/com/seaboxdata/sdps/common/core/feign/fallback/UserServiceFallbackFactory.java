package com.seaboxdata.sdps.common.core.feign.fallback;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;

import com.seaboxdata.sdps.common.core.feign.UserService;
import com.seaboxdata.sdps.common.core.model.LoginAppUser;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * userService降级工场
 *
 */
@Slf4j
public class UserServiceFallbackFactory implements FallbackFactory<UserService> {
	@Override
	public UserService create(Throwable throwable) {
		return new UserService() {
			@Override
			public SysUser selectByUsername(String username) {
				log.error("通过用户名查询用户异常:{}", username, throwable);
				return new SysUser();
			}

			@Override
			public LoginAppUser findByUsername(String username) {
				log.error("通过用户名查询用户异常:{}", username, throwable);
				return new LoginAppUser();
			}

			@Override
			public LoginAppUser findByMobile(String mobile) {
				log.error("通过手机号查询用户异常:{}", mobile, throwable);
				return new LoginAppUser();
			}

			@Override
			public LoginAppUser findByOpenId(String openId) {
				log.error("通过openId查询用户异常:{}", openId, throwable);
				return new LoginAppUser();
			}

			@Override
			public Result<Map<String, String>> serverLogin(String clusterId,
					String type, String username, Boolean isCache) {
				return Result.failed("操作失败");
			}
			@Override
			public SysUser selectByUserId(@RequestParam String userId){
				log.error("通过userId查询用户异常:{}", userId, throwable);
				return new LoginAppUser();
			}
		};
	}
}
