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

	// ??????????????????
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
	 * ?????????????????? LoginAppUser
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
	 * ??????or??????
	 *
	 * @param sysUser
	 * @return
	 */
	@PostMapping("/users/saveOrUpdate")
	@AuditLog(operation = "'?????????????????????:' + #sysUser.username")
	public Result saveOrUpdate(@RequestBody SysUser sysUser) throws Exception {
		return appUserService.saveOrUpdateUser(sysUser);
	}

	/**
	 * ????????????????????????SysUser
	 */
	@GetMapping(value = "/users/name/{username}")
	public SysUser selectByUsername(@PathVariable String username) {
		return appUserService.selectByUsername(username);
	}

	/**
	 * ????????????id????????????????????????SysUser
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
	 * ????????????????????????
	 */
	@PostMapping(value = "/users/password")
	public Result resetPassword(@RequestBody SysUser sysUser) {
		return appUserService.updatePassword(sysUser.getId(),
				sysUser.getOldPassword(), sysUser.getNewPassword(),sysUser.getSyncUser());
	}

	/**
	 * ????????????
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
		return Result.succeed(result, "????????????");
	}

	@PostMapping(value = "/sendValidationEmail")
	public Result sendValidationEmail(@RequestBody UserRequest request)
			throws MessagingException {
		if (StrUtil.isBlank(request.getEmail())
				|| StrUtil.isBlank(request.getDeviceId())
				|| StrUtil.isBlank(request.getValidCode())) {
			return Result.failed("?????????????????????");
		}
		Result checkResult = uaaFeginService.checkCode(request.getDeviceId(),
				request.getValidCode());
		if (checkResult.getCode() != 0) {
			return Result.failed("???????????????????????????");
		}
		ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
				.cast(RequestContextHolder.getRequestAttributes());
		HttpServletRequest httpRequest = requestAttributes.getRequest();
		Result result = null;
		SysUser userDao = appUserService.findUserByEmail(request.getEmail());
		if (userDao == null) {
			result = Result.failed("??????????????????");
		} else if (StrUtil.isBlank(userDao.getEmail())) {
			result = Result.failed("???????????????????????????");
		} else {
			if (validateService.sendValidateLimitation(userDao.getEmail(), 20,
					1)) {
				// ??????????????????????????????pm_validate?????????????????????????????????token
				ValidateDao validateDao = new ValidateDao();
				validateService.insertNewResetRecord(validateDao, userDao, UUID
						.randomUUID().toString());
				// ??????????????????
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				// multipart??????
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
						mimeMessage, true, "utf-8");
				mimeMessageHelper.setTo(userDao.getEmail());
				mimeMessageHelper.setFrom(from);
				mimeMessageHelper.setSubject("????????????");
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head>");
				sb.append("<body><h1>?????????????????????????????????</h1>" + "<a href = "
						+ resetPassUrl + "/resetPassword?token="
						+ validateDao.getResetToken() + ">" + resetPassUrl
						+ "/resetPassword?token=" + validateDao.getResetToken()
						+ "</a></body>");
				sb.append("</html>");
				// ??????html
				mimeMessageHelper.setText(sb.toString(), true);
				validateService.sendPasswordResetEmail(mimeMessage);
				result = Result.succeed("??????????????????");
			} else {
				result = Result.failed("???????????????????????????????????????");
			}
		}
		return result;
	}

	@PostMapping(value = "/resetPassword")
	public Result resetPassword(@RequestBody UserRequest request) {
		if (StrUtil.isBlank(request.getToken())
				|| StrUtil.isBlank(request.getConfirmPassword())
				|| StrUtil.isBlank(request.getPassword())) {
			return Result.failed("?????????????????????");
		}
		Result restResult = null;
		// ??????token??????validate??????
		ValidateDao validateDao = validateService.findUserByResetToken(request
				.getToken());
		if (validateDao == null) {
			restResult = Result.failed("????????????????????????");
		} else {
			if (validateService.validateLimitation(validateDao.getEmail(),
					Long.MAX_VALUE, 5, request.getToken())) {
				Long userId = validateDao.getUserId();
				if (request.getPassword().equals(request.getConfirmPassword())) {
					if (!request.getPassword().matches(
							SecurityConstants.PASS_REG)) {
						throw new BusinessException("??????????????????????????????????????????");
					}
					SysUser userDao = new SysUser();
					userDao.setPassword(passwordEncoder.encode(request
							.getPassword()));
					userDao.setId(userId);
					appUserService.update(
							userDao,
							new UpdateWrapper<SysUser>().eq("id",
									userDao.getId()));
					restResult = Result.succeed("??????????????????");
				} else {
					restResult = Result.failed("????????????????????????????????????????????????");
				}
			} else {
				restResult = Result.failed("???????????????");
			}
		}
		return restResult;
	}
}
