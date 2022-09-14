package com.seaboxdata.sdps.uaa.oauth.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.seaboxdata.sdps.common.core.constant.SecurityConstants;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.uaa.oauth.service.IValidateCodeService;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.utils.CaptchaUtil;

/**
 * 验证码提供
 * 
 * @author zlt
 * @date 2018/12/18
 */
@Controller
public class ValidateCodeController {
	@Autowired
	private IValidateCodeService validateCodeService;

	/**
	 * 创建验证码
	 *
	 * @throws Exception
	 */
	@GetMapping(SecurityConstants.DEFAULT_VALIDATE_CODE_URL_PREFIX
			+ "/{deviceId}")
	public void createCode(@PathVariable String deviceId,
			HttpServletResponse response) throws Exception {
		Assert.notNull(deviceId, "机器码不能为空");
		// 设置请求头为输出图片类型
		CaptchaUtil.setHeader(response);
		ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 35);
		validateCodeService.saveImageCode(deviceId, captcha.text()
				.toLowerCase());
		// 输出图片流
		captcha.out(response.getOutputStream());
	}

	/**
	 * 创建验证码
	 *
	 * @throws Exception
	 */
	@GetMapping(SecurityConstants.RESET_PASSWORD_VALIDATE_CODE_URL_PREFIX
			+ "/{deviceId}")
	public void createResetPasswordCode(@PathVariable String deviceId,
			HttpServletResponse response) throws Exception {
		Assert.notNull(deviceId, "机器码不能为空");
		// 设置请求头为输出图片类型
		CaptchaUtil.setHeader(response);
		SpecCaptcha captcha = new SpecCaptcha(100, 35);
		captcha.setLen(4);
		validateCodeService.saveImageCode(deviceId, captcha.text()
				.toLowerCase());
		// 输出图片流
		captcha.out(response.getOutputStream());
	}

	/**
	 * 发送手机验证码 后期要加接口限制
	 *
	 * @param mobile
	 *            手机号
	 * @return R
	 */
	@ResponseBody
	@GetMapping(SecurityConstants.MOBILE_VALIDATE_CODE_URL_PREFIX + "/{mobile}")
	public Result createCode(@PathVariable String mobile) {
		Assert.notNull(mobile, "手机号不能为空");
		return validateCodeService.sendSmsCode(mobile);
	}

	@ResponseBody
	@GetMapping("/check/code")
	public Result checkCode(@RequestParam("deviceId") String deviceId,
			@RequestParam("validCode") String validCode) {
		Assert.notNull(validCode, "验证码不能为空");
		Assert.notNull(deviceId, "机器码不能为空");
		validateCodeService.validate(deviceId, validCode);
		return Result.succeed("操作成功");
	}

}
