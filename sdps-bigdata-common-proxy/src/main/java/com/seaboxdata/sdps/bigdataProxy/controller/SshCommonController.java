package com.seaboxdata.sdps.bigdataProxy.controller;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ssh")
public class SshCommonController {

    @PostMapping("/login")
    public Result<JSONObject> login(@RequestParam("ip") String ip,
                                    @RequestParam("name") String name, @RequestParam("password") String password) {
        if (StringUtils.isBlank(ip.trim())) {
            return Result.succeed("IP不可为空");
        }
        if (StringUtils.isBlank(name.trim())) {
            return Result.succeed("用户名不可为空");
        }
        if (StringUtils.isBlank(password.trim())) {
            return Result.succeed("密码不可为空");
        }
        RemoteShellExecutorUtil shellExecutorUtil = new RemoteShellExecutorUtil(ip, name, password);
        boolean loginResult = shellExecutorUtil.login();
        return loginResult ? Result.succeed("连接成功") : Result.succeed("连接失败");
    }

}
