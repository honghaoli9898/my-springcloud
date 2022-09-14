package com.seaboxdata.sdps.uaa.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seaboxdata.sdps.common.crypto.annotation.DigestsCrypto;
import com.seaboxdata.sdps.common.crypto.constants.DigestsType;
import com.seaboxdata.sdps.common.crypto.constants.EncodingType;

/**
 * 摘要 加密
 */

// 全部方法加密
//@DigestsCrypto(type = DigestsType.MD5)

@RestController
public class DigestController {

    /**
     * 使用 MD5 加密响应
     */
    @DigestsCrypto(type = DigestsType.MD5)
    @GetMapping("/md5")
    public String md5() {
        return "md5 test!";

    }

    /**
     * 使用 SHA512 加密响应
     */
    @DigestsCrypto(type = DigestsType.SHA512)
    @GetMapping("/sha512")
    public String sha512() {
        return "SHA512 test!";

    }

    /**
     * 使用 SHA512 加密响应
     */
    @DigestsCrypto(type = DigestsType.SHA512, encodingType = EncodingType.HEX)
    @GetMapping("/base64")
    public String sha512Base64() {
        return "sha512 Base64 test!";

    }
}
