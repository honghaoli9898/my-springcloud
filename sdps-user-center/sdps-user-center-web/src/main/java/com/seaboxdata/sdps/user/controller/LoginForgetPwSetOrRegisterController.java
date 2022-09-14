package com.seaboxdata.sdps.user.controller;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.user.api.ILoginForgetPwAndRegisterService;
import com.seaboxdata.sdps.user.api.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@RestController
@RequestMapping("/forgetPwSetOrRegister")
public class LoginForgetPwSetOrRegisterController {

    //创建线程池对象
    //ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);

    @Autowired
    private ILoginForgetPwAndRegisterService loginForgetPwAndRegisterService;

    @Autowired
    private IUserService appUserService;

    @GetMapping(value = "/sendMail", params = "toEmail")
    public Result sendMail(String toEmail) {



        if (null == toEmail) {
            return Result.failed("邮箱不能为空。");
        } else {
            return loginForgetPwAndRegisterService.sendVcodeMail(toEmail);
        }

    }

    @GetMapping(value = "/checkVcode", params = "toEmail")
    public Result codeMailCheck(String toEmail, String verCode) {

        loginForgetPwAndRegisterService.codeMailCheck(toEmail, verCode);
        return Result.succeed("校验成功！");
    }


    @PostMapping(value = "/reSetPw", params = "toEmail")
    public Result reSetPw(String toEmail, String username,
                          String password,
                          String verCode) {

        loginForgetPwAndRegisterService.reSetPw(toEmail, username, password, verCode);

        return Result.succeed("重置密码成功！");
    }

    @PostMapping(value = "/registerUser", params = "toEmail")
    public Result registerUser(String toEmail, String username,
                               String password,
                               String verCode) {

        Result rs= loginForgetPwAndRegisterService.registerUser(toEmail, username, password, verCode,appUserService);

        return rs;
    }
}
