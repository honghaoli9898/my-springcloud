package com.seaboxdata.sdps.uaa.example.controller;

import java.util.Collections;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.seaboxdata.sdps.common.crypto.annotation.SignatureCrypto;
import com.seaboxdata.sdps.uaa.example.bean.TestBean;

/**
 * 接口数据签名、验签
 */
@RestController
public class SignatureController {

	// @NotDecrypt // 不需要解密
	// @NotEncrypt // 不需要加密
	@SignatureCrypto(timeout = 15)
	@PostMapping("/signature")
	public TestBean signature(@RequestBody TestBean req) {

		System.out.println(req.toString());

		TestBean testBean = new TestBean();
		testBean.setAnInt(0);
		testBean.setInteger(1);
		testBean.setString("test string");
		testBean.setStringList(Collections.singletonList("list"));
		testBean.setObjectMap(Collections.singletonMap("test", "map"));

		return testBean;
	}

}
