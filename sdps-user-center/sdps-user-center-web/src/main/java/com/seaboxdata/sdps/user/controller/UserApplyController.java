package com.seaboxdata.sdps.user.controller;


import cn.hutool.core.util.StrUtil;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.user.api.IUserApplyService;
import com.seaboxdata.sdps.user.api.IUserApprovalService;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.mybatis.dto.user.UserApplyDto;
import com.seaboxdata.sdps.user.mybatis.model.UserApply;
import com.seaboxdata.sdps.user.vo.user.ApplyVo;
import com.seaboxdata.sdps.user.vo.user.PageApplyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/USQ10")
public class UserApplyController {

    @Autowired
    private IUserApplyService userApplyService;

    @Autowired
    private IUserApprovalService userApprovalService;

    @Autowired
    private IUserService appUserService;

    /**
     * 应用申请
     * @param userApply
     * @return
     */
    @PostMapping("/USQ1001")
    public Result applyApp(@RequestBody UserApply userApply) {
        if (Objects.isNull(userApply) || StrUtil.isBlank(userApply.getApplyReason()) || userApply.getApplyReason().length() < 5) {
            return Result.failed("申请原因不能为空（至少5个字符）!");
        }

        userApplyService.addUserApplyInfo(userApply);

        return Result.succeed("申请成功，请等待管理员审批！");
    }


    /**
     * 审批管理-我的申请（查询当前用户的申请表单信息）
     * @param user
     * @return
     */
    @GetMapping("/USQ1002")
    public  Result myApply(@LoginUser(isFull = true) SysUser user) {
        // 获取当前用户
        String userName = user.getUsername();
        List<UserApplyDto> userApplies = userApplyService.selectUserApplyInfo(userName);
        return Result.succeed(userApplies);
    }

    /**
     * 审批管理-我的申请(筛选查询功能（null 未选择，0全部，1待办，2已完成）)
     * 申请内容（模糊查询）
     * 状态（null 未选择，0待审批，1已通过，2已驳回）
     */
    @PostMapping("/USQ1003")
    public PageResult<UserApplyDto> filterSelect(@LoginUser(isFull = true) SysUser user,
//                                          @RequestParam(value = "status",required = false) Integer status ,
//                                          @RequestParam(value = "applyContent",required = false) String applyContent,
//                                          @RequestParam(value = "spStatus",required = false) Integer spStatus
                                                      @Validated @RequestBody PageApplyRequest pageApplyRequest
                                 ) {
        // 获取当前用户
        pageApplyRequest.setUserName(user.getUsername());
        PageResult<UserApplyDto> userApplies = userApplyService.selectUserApplyInfoPage(pageApplyRequest);


        return userApplies;
    }

    /**
     * 再次申请功能
     */
    @PostMapping("/USQ1004")
    public Result reApply(@RequestBody ApplyVo applyVo) {
        if (Objects.isNull(applyVo) || StrUtil.isBlank(applyVo.getApplyReason()) || applyVo.getApplyReason().length() < 5) {
            return Result.failed("申请原因不能为空（至少5个字符）!");
        }

        userApplyService.updateUserApplyInfo(applyVo);

        return Result.succeed("申请成功，请等待管理员审批！");
    }


}
