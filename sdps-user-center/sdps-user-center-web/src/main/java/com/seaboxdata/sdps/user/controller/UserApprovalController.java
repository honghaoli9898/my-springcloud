package com.seaboxdata.sdps.user.controller;


import cn.hutool.core.util.StrUtil;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.user.api.IUserApplyService;
import com.seaboxdata.sdps.user.api.IUserApprovalService;
import com.seaboxdata.sdps.user.mybatis.model.UserApproval;
import com.seaboxdata.sdps.user.vo.user.ApplyVo;
import com.seaboxdata.sdps.user.vo.user.ApprovalVo;
import com.seaboxdata.sdps.user.vo.user.PageApplyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/USP10")
public class UserApprovalController {

    @Autowired
    private IUserApplyService userApplyService;

    @Autowired
    private IUserApprovalService userApprovalService;

    /**
     * 审批管理-我的审批（查询当前用户的审批表单信息）
     * @param user
     * @return
     */
    @GetMapping("/USP1001")
    public  Result myApproval(@LoginUser(isFull = true) SysUser user) {
        // 获取当前用户
        String userName = user.getUsername();
        List<UserApproval> userApprovals = userApprovalService.selectUserApprovalInfo(userName);
        return Result.succeed(userApprovals);
    }

    /**
     * 审批管理-我的审批(筛选查询功能（null 未选择，0全部，1待办，2已完成）)
     * 申请内容（模糊查询）
     * 状态（null 未选择，0待审批，1已通过，2已驳回）
     * 分页查询
     */
    @PostMapping("/USP1002")
    public PageResult<UserApproval> filterSelect(@LoginUser(isFull = true) SysUser user,
                                   @Validated @RequestBody PageApplyRequest pageApplyRequest) {

        // 获取当前用户
        String userName = user.getUsername();
        pageApplyRequest.setUserName(userName);
        PageResult<UserApproval> userApprovalPageResult = userApprovalService.selectUserApprovalInfoPage(pageApplyRequest);

        return userApprovalPageResult;

    }

    /**
     * 审批管理-我的审批（审批功能）
     * 0-无操作，1-通过，2-驳回
     */
    @PostMapping("/USP1003")
    public Result approvalProcess(@RequestBody ApprovalVo approvalVo) {
        if (Objects.isNull(approvalVo) || StrUtil.isBlank(approvalVo.getSpReason()) || approvalVo.getSpReason().length() == 0 ) {
            return Result.failed("请填写原因");
        }
        // 获取当前用户
        userApprovalService.updateApprovalInfo(approvalVo);

        return Result.succeed("审批成功");
    }


    /**
     * 新增审批
     */
    @PostMapping("/addAppoval")
    public Result addAppoval(@RequestBody @Validated ApprovalVo approvalVo,@LoginUser(isFull = true) SysUser user) {
        // 获取当前用户
        return Result.succeed(userApprovalService.addApproval(approvalVo, user));
    }

}
