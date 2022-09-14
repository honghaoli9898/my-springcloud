package com.seaboxdata.sdps.bigdataProxy.controller;

import com.seaboxdata.sdps.bigdataProxy.service.ExternalRoleService;
import com.seaboxdata.sdps.common.core.model.ExternalRole;
import com.seaboxdata.sdps.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 外部角色控制器
 * @author pengsong
 */
@RestController
@RequestMapping("/externalRole")
@Slf4j
public class ExternalRoleController{

    @Autowired
    private ExternalRoleService externalRoleService;

    @PostMapping("/getExternalRoles")
    public Result getExternalRoles(@RequestBody ExternalRole role) {
        return Result.succeed(externalRoleService.getExternalRoles(role));
    }

    @PostMapping("/getExternalSystem")
    public Result getExternalSystem() {
        return Result.succeed(externalRoleService.getExternalSystem());
    }
}
