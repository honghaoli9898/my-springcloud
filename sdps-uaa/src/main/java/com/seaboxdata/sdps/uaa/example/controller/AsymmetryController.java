package com.seaboxdata.sdps.uaa.example.controller;

import java.util.Collections;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.seaboxdata.sdps.common.crypto.annotation.AsymmetryCrypto;
import com.seaboxdata.sdps.common.crypto.constants.AsymmetryType;
import com.seaboxdata.sdps.uaa.example.bean.TestBean;

/**
 * 非对称性加密、解密
 */
@RestController
public class AsymmetryController {


//    @NotEncrypt // 不需要加密
    @AsymmetryCrypto(
            // 响应内容签名
            signature = true,
            // 请求内容验证签名
            verifySignature = true,
            // 加密、解密算法
            type = AsymmetryType.RSA_ECB_PKCS1_PADDING
    )
    @PostMapping("/testBean1")
    public TestBean testBean1(@RequestBody TestBean req) throws Exception {

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
