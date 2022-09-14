package com.seaboxdata.sdps.uaa.oauth.granter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.captcha.model.common.ResponseModel;
import com.seaboxdata.sdps.common.captcha.model.vo.CaptchaVO;
import com.seaboxdata.sdps.common.captcha.service.CaptchaService;
import com.seaboxdata.sdps.uaa.oauth.exception.ValidateCodeException;
import com.seaboxdata.sdps.uaa.oauth.service.IValidateCodeService;

/**
 * password添加图像验证码授权模式
 *
 * @author zlt
 * @date 2020/7/11
 *       <p>
 *       Blog: https://zlt2000.gitee.io Github: https://github.com/zlt2000
 */
public class PwdImgCodeGranter extends ResourceOwnerPasswordTokenGranter {
	private static final String GRANT_TYPE = "password_code";

	private final IValidateCodeService validateCodeService;

	private CaptchaService captchaService;

	public PwdImgCodeGranter(AuthenticationManager authenticationManager,
			AuthorizationServerTokenServices tokenServices,
			ClientDetailsService clientDetailsService,
			OAuth2RequestFactory requestFactory,
			IValidateCodeService validateCodeService,
			CaptchaService captchaService) {
		super(authenticationManager, tokenServices, clientDetailsService,
				requestFactory, GRANT_TYPE);
		this.validateCodeService = validateCodeService;
		this.captchaService = captchaService;
	}

	@Override
	protected OAuth2Authentication getOAuth2Authentication(
			ClientDetails client, TokenRequest tokenRequest) {
		Map<String, String> parameters = new LinkedHashMap<>(
				tokenRequest.getRequestParameters());
		String deviceId = parameters.get("deviceId");
		String validCode = parameters.get("validCode");
		String captchaVerification = parameters.get("captchaVerification");
		if (StrUtil.isNotBlank(deviceId) && StrUtil.isNotBlank(validCode)) {
			validateCodeService.validate(deviceId, validCode);
		} else {
			CaptchaVO captchaVO = new CaptchaVO();
			captchaVO.setCaptchaVerification(captchaVerification);
			ResponseModel response = captchaService.verification(captchaVO);
			if (!response.isSuccess()) {
				 throw new ValidateCodeException("验证码不正确");
			}
		}

		return super.getOAuth2Authentication(client, tokenRequest);
	}
}
