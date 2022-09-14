package com.seaboxdata.sdps.uaa.example.controller;

import java.util.Collections;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaboxdata.sdps.common.crypto.annotation.Decrypt;
import com.seaboxdata.sdps.common.crypto.annotation.Encrypt;
import com.seaboxdata.sdps.common.crypto.annotation.SymmetricCrypto;
import com.seaboxdata.sdps.common.crypto.constants.SymmetricType;
import com.seaboxdata.sdps.uaa.example.bean.TestBean;

/**
 * 对称性加密、解密
 */
@RestController
public class SymmetricController {

	@Resource
	ObjectMapper objectMapper;

	@Decrypt
	@Encrypt
	@SymmetricCrypto(type = SymmetricType.AES_CFB_PKCS7_PADDING)
	@PostMapping("/symmetric")
	public String symmetric(@RequestBody TestBean req)
			throws JsonProcessingException {

		System.out.println(req.toString());

		TestBean testBean = new TestBean();
		testBean.setAnInt(0);
		testBean.setInteger(1);
		testBean.setString("test string");
		testBean.setStringList(Collections.singletonList("list"));
		testBean.setObjectMap(Collections.singletonMap("test", "map"));

		return objectMapper.writeValueAsString(testBean);
	}
}
