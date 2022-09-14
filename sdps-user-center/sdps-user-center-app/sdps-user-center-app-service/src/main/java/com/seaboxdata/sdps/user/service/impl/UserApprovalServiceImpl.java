package com.seaboxdata.sdps.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.constant.UrlConstants;
import com.seaboxdata.sdps.common.core.model.ExternalRole;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.RoleExternal;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IUserApprovalService;
import com.seaboxdata.sdps.user.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.user.mybatis.mapper.UserApplyMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.UserApprovalMapper;
import com.seaboxdata.sdps.user.mybatis.model.UserApproval;
import com.seaboxdata.sdps.user.vo.user.ApprovalVo;
import com.seaboxdata.sdps.user.vo.user.PageApplyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserApprovalServiceImpl extends SuperServiceImpl<UserApprovalMapper, UserApproval> implements IUserApprovalService {




    @Autowired
    public UserApprovalMapper userApprovalMapper;

    @Autowired
    public UserApplyMapper userApplyMapper;

    @Autowired
    private BigdataCommonFegin bigdataCommonFegin;


    /**
     * 审批表单信息查询
     * @param userName
     * @return
     */
    @Override
    public List<UserApproval> selectUserApprovalInfo(String userName) {
        // List<UserApproval> userApprovals = userApprovalMapper.selectInfoByUser(userName);


        QueryWrapper<UserApproval> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sp_username", userName);
        queryWrapper.orderByAsc("sp_status");
        queryWrapper.orderByDesc("apply_time");
        List<UserApproval> userApprovals = this.list(queryWrapper);


        return userApprovals;
    }

    /**
     * 我的审批：筛选查询
     * @param userName
     * @param status
     * @param applyContent
     * @param spStatus
     * @return
     */
    @Override
    public List<UserApproval> selectUserApprovalInfoByWhere(String userName, Integer status, String applyContent, Integer spStatus) {
        List<Integer> param = null;

        if (Objects.nonNull(status)) {
            param =  CollUtil.newArrayList();
            if (status == 0) {
                param.add(0);
                param.add(1);
                param.add(2);
            }

            if (status == 1) {
                param.add(0);
            }
            if (status == 2) {
                param.add(1);
                param.add(2);
            }
        }

        List<UserApproval> userApprovals = userApprovalMapper.selectInfoByWhere(userName, param, applyContent, spStatus);
        return userApprovals;
    }

    /**
     * 分页查询
     * @param pageApplyRequest
     * @return
     */
    @Override
    public PageResult<UserApproval> selectUserApprovalInfoPage(PageApplyRequest pageApplyRequest) {
        Integer status = pageApplyRequest.getStatus();
        List<Integer> params = null;
        if (Objects.nonNull(status)) {
            params = CollUtil.newArrayList();
            if (status == 0) {
                params.add(0);
                params.add(1);
                params.add(2);
            }

            if (status == 1) {
                params.add(0);
            }

            if (status == 2) {
                params.add(1);
                params.add(2);
            }

        }


        int pageSize = pageApplyRequest.getPageSize();
        int pageNum = pageApplyRequest.getPageNum();
        PageHelper.startPage(pageNum, pageSize);
        Page<UserApproval> userApprovals = userApprovalMapper.selectInfoPage(pageApplyRequest.getUserName(), params, pageApplyRequest.getApplyContent()
                , pageApplyRequest.getSpStatus());

        return PageResult.<UserApproval> builder().code(0).data(userApprovals.getResult())
                .count(userApprovals.getTotal()).msg("查询成功").build();
    }

    /**
     * 审批管理-我的审批(审批功能)
     */
    @Override
    public void updateApprovalInfo(ApprovalVo approvalVo) {
        QueryWrapper<UserApproval> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", approvalVo.getSpId());
        UserApproval userApproval = userApprovalMapper.selectOne(queryWrapper);

        userApproval.setSpStatus(approvalVo.getAction());
        userApproval.setSpTime(new Date());
        userApproval.setAction(approvalVo.getAction());
        userApproval.setSpReason(approvalVo.getSpReason());

        UpdateWrapper<UserApproval> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", approvalVo.getSpId());
        this.update(userApproval, updateWrapper);


    }

    @Override
    @Transactional
    public Boolean addApproval(ApprovalVo approvalVo, SysUser user) {

        //权限申请
        UserApproval userApproval = new UserApproval();
        //urlId
        userApproval.setSqId(approvalVo.getSpId());
        userApproval.setApplyContent(approvalVo.getName());
        userApproval.setSpUserId(user.getId());
        userApproval.setSpUserName(user.getUsername());
        userApproval.setSqUserId(1L);
        userApproval.setSqUserName(CommonConstant.ADMIN_USER_NAME);
        userApproval.setRole(approvalVo.getRole());
        userApproval.setRoleId(approvalVo.getRoleId());
        userApproval.setApplyReason(approvalVo.getSpReason());
        userApproval.setApplyTime(new Date());
        userApproval.setSpStatus(UrlConstants.APPLYING_STATUS);
        userApprovalMapper.insert(userApproval);
       //插入url role关系
        RoleExternal roleExternal = new RoleExternal();
        roleExternal.setVerifyStatus(UrlConstants.APPLYING_STATUS);
        roleExternal.setCreator(user.getUsername());
        roleExternal.setUsername(user.getUsername());
        roleExternal.setCreateTime(new Date());
        roleExternal.setDescription(approvalVo.getSpReason());
        roleExternal.setUrlId(approvalVo.getSpId());
        roleExternal.setExternalId(approvalVo.getRoleId());
        roleExternal.setCreateTime(new Date());
        roleExternal.setCreator(user.getUsername());

        Result result = bigdataCommonFegin.addRoleRelation(roleExternal);
        log.info(result.getMsg());
        return true;
    }


}
