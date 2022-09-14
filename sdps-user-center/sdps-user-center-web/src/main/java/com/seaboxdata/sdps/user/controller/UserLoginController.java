package com.seaboxdata.sdps.user.controller;

import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.LoginAppUser;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.log.annotation.AuditLog;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.feign.UaaFeginService;
import com.seaboxdata.sdps.user.mybatis.model.ValidateDao;
import com.seaboxdata.sdps.user.mybatis.vo.user.UserRequest;
import com.seaboxdata.sdps.user.service.impl.ValidateService;

@RestController
public class UserLoginController {
	@Autowired
	private IUserService appUserService;
	@Autowired
	private ValidateService validateService;
	@Autowired
	private UaaFeginService uaaFeginService;

	// 发送邮件的类
	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.resetPassUrl}")
	private String resetPassUrl;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${spring.mail.username}")
	private String from;

	@GetMapping(value = "/users-anon/login", params = "username")
	public LoginAppUser findByUsername(String username) {
		return appUserService.findByUsername(username);
	}

	/**
	 * 当前登录用户 LoginAppUser
	 *
	 * @return
	 */
	@GetMapping("/users/current")
	public Result<LoginAppUser> getLoginAppUser(
			@LoginUser(isFull = true) SysUser user) {
		LoginAppUser loginAppUser = appUserService.getLoginAppUser(user);
		loginAppUser.setPassword(null);
		return Result.succeed(loginAppUser);
	}

	/**
	 * 新增or更新
	 *
	 * @param sysUser
	 * @return
	 */
	@PostMapping("/users/saveOrUpdate")
	@AuditLog(operation = "'新增或更新用户:' + #sysUser.username")
	public Result saveOrUpdate(@RequestBody SysUser sysUser) throws Exception {
		return appUserService.saveOrUpdateUser(sysUser);
	}

	/**
	 * 查询用户实体对象SysUser
	 */
	@GetMapping(value = "/users/name/{username}")
	public SysUser selectByUsername(@PathVariable String username) {
		return appUserService.selectByUsername(username);
	}

	/**
	 * 根据用户id查询用户实体对象SysUser
	 */
	@GetMapping(value = "/users/name")
	public SysUser selectByuserId(@RequestParam String userId) {
		return appUserService.selectByUserId(userId);
	}

	@PostMapping(value = "/users/{id}/password")
	public Result resetPassword(@PathVariable Long id,
			@RequestParam(name = "password", required = false) String password,
			@RequestParam(name = "password", required = false) Boolean syncUser) {
		return appUserService.updatePassword(id, null, password,syncUser);
	}

	/**
	 * 用户自己修改密码
	 */
	@PostMapping(value = "/users/password")
	public Result resetPassword(@RequestBody SysUser sysUser) {
		return appUserService.updatePassword(sysUser.getId(),
				sysUser.getOldPassword(), sysUser.getNewPassword(),sysUser.getSyncUser());
	}

	/**
	 * 组件登录
	 * 
	 * @return
	 */
	@GetMapping(value = "/server/login")
	public Result<Map<String, String>> serverLogin(
			@RequestParam("clusterId") String clusterId,
			@RequestParam("type") String type,
			@RequestParam(value = "username") String username,
			@RequestParam(value = "isCache") Boolean isCache) {
		Map<String, String> result = appUserService.serverLogin(clusterId,
				type, username, isCache);
		return Result.succeed(result, "操作成功");
	}

	@PostMapping(value = "/sendValidationEmail")
	public Result sendValidationEmail(@RequestBody UserRequest request)
			throws MessagingException {
		if (StrUtil.isBlank(request.getEmail())
				|| StrUtil.isBlank(request.getDeviceId())
				|| StrUtil.isBlank(request.getValidCode())) {
			return Result.failed("请检查传入参数");
		}
		Result checkResult = uaaFeginService.checkCode(request.getDeviceId(),
				request.getValidCode());
		if (checkResult.getCode() != 0) {
			return Result.failed("验证码不正确或过期");
		}
		ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
				.cast(RequestContextHolder.getRequestAttributes());
		HttpServletRequest httpRequest = requestAttributes.getRequest();
		Result result = null;
		SysUser userDao = appUserService.findUserByEmail(request.getEmail());
		if (userDao == null) {
			result = Result.failed("该用户不存在");
		} else if (StrUtil.isBlank(userDao.getEmail())) {
			result = Result.failed("该用户没有绑定邮箱");
		} else {
			if (validateService.sendValidateLimitation(userDao.getEmail(), 20,
					1)) {
				// 若允许重置密码，则在pm_validate表中插入一行数据，带有token
				ValidateDao validateDao = new ValidateDao();
				validateService.insertNewResetRecord(validateDao, userDao, UUID
						.randomUUID().toString());
				// 设置邮件内容
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				// multipart模式
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
						mimeMessage, true, "utf-8");
				mimeMessageHelper.setTo(userDao.getEmail());
				mimeMessageHelper.setFrom(from);
				mimeMessageHelper.setSubject("重置密码");
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head>");
				sb.append("<body><h1>点击下面的链接重置密码</h1>" + "<a href = "
						+ resetPassUrl + "/resetPassword?token="
						+ validateDao.getResetToken() + ">" + resetPassUrl
						+ "/resetPassword?token=" + validateDao.getResetToken()
						+ "</a></body>");
				sb.append("</html>");
				// 启用html
				mimeMessageHelper.setText(sb.toString(), true);
				validateService.sendPasswordResetEmail(mimeMessage);
				result = Result.succeed("邮件已经发送");
			} else {
				result = Result.failed("操作过于频繁，请稍后再试！");
			}
		}
		return result;
	}

	@PostMapping(value = "/resetPassword")
	public Result resetPassword(@RequestBody UserRequest request) {
		if (StrUtil.isBlank(request.getToken())
				|| StrUtil.isBlank(request.getConfirmPassword())
				|| StrUtil.isBlank(request.getPassword())) {
			return Result.failed("请检查传入参数");
		}
		Result restResult = null;
		// 通过token找到validate记录
		ValidateDao validateDao = validateService.findUserByResetToken(request
				.getToken());
		if (validateDao == null) {
			restResult = Result.failed("该重置请求不存在");
		} else {
			if (validateService.validateLimitation(validateDao.getEmail(),
					Long.MAX_VALUE, 5, request.getToken())) {
				Long userId = validateDao.getUserId();
				if (request.getPassword().equals(request.getConfirmPassword())) {
					if (!request.getPassword().matches(
							SecurityConstants.PASS_REG)) {
						throw new BusinessException("密码必须包含数字字母特殊字符");
					}
					SysUser userDao = new SysUser();
					userDao.setPassword(passwordEncoder.encode(request
							.getPassword()));
					userDao.setId(userId);
					appUserService.update(
							userDao,
							new UpdateWrapper<SysUser>().eq("id",
									userDao.getId()));
					restResult = Result.succeed("成功修改密码");
				} else {
					restResult = Result.failed("确认密码和密码不一致，请重新输入");
				}
			} else {
				restResult = Result.failed("该链接失效");
			}
		}
		return restResult;
	}
}
