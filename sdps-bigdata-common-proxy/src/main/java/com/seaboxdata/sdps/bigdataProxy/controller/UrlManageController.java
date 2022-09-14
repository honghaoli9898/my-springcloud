package com.seaboxdata.sdps.bigdataProxy.controller;

import com.seaboxdata.sdps.bigdataProxy.service.UrlManageService;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.RoleExternal;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.model.Url;
import com.seaboxdata.sdps.common.core.model.UrlVo;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/url")
@Slf4j
public class UrlManageController{
    @Autowired
    private UrlManageService urlManageService;

    /**
     * 新增链接
     * @param url
     * @param user
     * @return
     */
    @PostMapping("/addUrl")
    public Result addUrl(@RequestBody @Valid Url url, @LoginUser SysUser user){
        return Result.succeed(urlManageService.addUrl(url ,user));
    }

    /**
     * 删除链接
     * @param id
     * @param user
     * @return
     */
    @PostMapping("/deleteUrl/{id}")
    public Result deleteUrl(@PathVariable("id") Long id, @LoginUser SysUser user){
        return Result.succeed(urlManageService.deleteUrl(id ,user));
    }


    /**
     * 编辑链接
     * @param url
     * @param user
     * @return
     */
    @PostMapping("/editUrl")
    public Result editUrl(@RequestBody @Valid Url url, @LoginUser SysUser user){
        return Result.succeed(urlManageService.editUrl(url ,user));
    }


    /**
     * 查询链接管理
     * @param url
     * @param user
     * @return
     */
    @PostMapping("/getUrl")
    public PageResult<Url> getUrl(@RequestBody PageRequest<Url> url, @LoginUser SysUser user) {
        PageResult<Url> res = urlManageService.getUrl(url, user);
        return res;
    }

    /**
     * 查询链接管理
     * @param url
     * @param user
     * @return
     */
    @PostMapping("/getAllUrl")
    public PageResult<UrlVo> getAllUrl(@RequestBody PageRequest<Url> url, @LoginUser SysUser user) {
        PageResult<UrlVo> res = urlManageService.getAllUrl(url, user);
        return res;
    }


    /**
     * 创建url role关系
     * @param role
     * @return
     */
    @PostMapping("/addRoleRelation")
    public Result addRoleRelation(@RequestBody RoleExternal role) {
        return Result.succeed(urlManageService.addRoleRelation(role));
    }

    /**
     * 创建url role关系
     * @param role
     * @return
     */
    @PostMapping("/addRoleRelations")
    public Result addRoleRelations(@RequestBody List<RoleExternal> role,@LoginUser SysUser user) {
        return Result.succeed(urlManageService.addRoleRelations(role, user));
    }

    /**
     * 查询链接管理
     * @param url
     * @return
     */
    @PostMapping("/getRoleRelation")
    public PageResult<RoleExternal> getRoleRelation(@RequestBody PageRequest<RoleExternal> url) {
        PageResult<RoleExternal> res = urlManageService.getRoleRelation(url);
        return res;
    }


    /**
     * 查询链接管理
     * @param url
     * @return
     */
    @PostMapping("/isSystemUrl")
    public Result<Object> isSystemUrl(@RequestBody RoleExternal url) {
        String res = urlManageService.isSystemUrl(url);
        return Result.succeed(res);
    }

    /**
     * 删除链接关系
     * @param id
     * @param user
     * @return
     */
    @PostMapping("/delRoleRelation/{id}")
    public Result delRoleRelation(@PathVariable("id") Long id, @LoginUser SysUser user) {
        return Result.succeed(urlManageService.delRoleRelation(id, user));
    }

    /**
     * 通过urlid获取关联用户
     * @param urlId
     * @return
     */
    @PostMapping("/getUsersByUrlId")
    public Set<String> getUsersByUrlId(@RequestParam("urlId") Long urlId) {
        return urlManageService.getUsersByUrlId(urlId);
    }

}
