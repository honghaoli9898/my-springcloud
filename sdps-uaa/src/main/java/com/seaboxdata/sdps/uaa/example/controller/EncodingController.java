package com.seaboxdata.sdps.uaa.example.controller;

import java.util.Collections;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.seaboxdata.sdps.common.crypto.annotation.EncodingCrypto;
import com.seaboxdata.sdps.common.crypto.constants.EncodingType;
import com.seaboxdata.sdps.uaa.example.bean.TestBean;

/**
 * 编码、解码
 */
@RestController
public class EncodingController {

	@EncodingCrypto(encodingType = EncodingType.BASE64)
	@PostMapping("/encoding")
	public TestBean encoding(@RequestBody TestBean req) {

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
