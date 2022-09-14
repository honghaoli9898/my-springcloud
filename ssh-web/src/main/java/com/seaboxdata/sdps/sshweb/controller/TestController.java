package com.seaboxdata.sdps.sshweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.seaboxdata.sdps.common.redis.template.RedisRepository;


@Controller
public class TestController{

    @Autowired
    private RedisRepository redisRepository;

    @GetMapping("/test")
    @ResponseBody
    public String test(){
        redisRepository.set("test","test");
        Object test = redisRepository.get("test");
        return (String)test;
    }

    @GetMapping("/test2")
    @ResponseBody
    public String test2(){
        redisRepository.set("test2","test2");
        Object test = redisRepository.get("test2");
        return (String)test;
    }

    public static void main(String[] args) {
        System.out.println("ceshitijiao");
    }
}
