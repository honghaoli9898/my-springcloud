package com.seaboxdata.sdps.uaa.example.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.seaboxdata.sdps.uaa.example.custom.CustomCrypto;

/**
 * 自定义模式控制器
 */
@RestController
public class CustomController {

    @CustomCrypto(isDecryption = true, isEncryption = true, salt = "123456")
    @PostMapping("/custom")
    public String custom(@RequestBody String text) {
        System.out.println(text);

        return "ABC123456";
    }
}
