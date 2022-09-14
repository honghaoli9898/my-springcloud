package com.seaboxdata.sdps.common.crypto.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;

import cn.hutool.core.io.IoUtil;

import com.seaboxdata.sdps.common.crypto.algorithm.ApiCryptoAlgorithm;
import com.seaboxdata.sdps.common.crypto.algorithm.SymmetricApiCrypto;
import com.seaboxdata.sdps.common.crypto.bean.ApiCryptoBody;

/**
 * 为了减少不必要的消耗，用户可根据自身需求注册不同的实现模式到容器以使用。 </p> 目前所有的已实现模式都实现于 `ApiCryptoAlgorithm`
 * 接口，需要什么模式将该接口其对应的实现类 @Bean 注入即可。
 */
@ConditionalOnClass({ HttpServletRequest.class, RequestContextHolder.class })
public class ApiCryptoConfiguration {

	/**
	 * 对称性加密解密 Bean
	 */
	@Bean
	public ApiCryptoAlgorithm symmetricApiCrypto() {
		SymmetricApiCrypto symmetricApiCrypto = new SymmetricApiCrypto();
		// 自定义请求内容解析
		symmetricApiCrypto.setiApiRequestBody((annotation, inputStream) -> {
			byte[] byteArr = IoUtil.readBytes(inputStream);
			String str = new String(byteArr);
			String[] strings = str.split("&");
			return new ApiCryptoBody().setData(strings[0]).setIv(strings[1]);
		});
		// 自定义响应内容格式
		symmetricApiCrypto.setiApiResponseBody((annotation, cryptoBody) -> {
			String data = cryptoBody.getData();
			String iv = cryptoBody.getIv();
			return data + "&" + iv;
		});
		return symmetricApiCrypto;
	}
}
