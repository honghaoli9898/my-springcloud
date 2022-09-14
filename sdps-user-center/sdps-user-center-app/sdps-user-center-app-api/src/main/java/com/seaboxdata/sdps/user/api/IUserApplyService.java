package com.seaboxdata.sdps.user.api;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.dto.user.UserApplyDto;
import com.seaboxdata.sdps.user.mybatis.model.UserApply;
import com.seaboxdata.sdps.user.vo.user.ApplyVo;
import com.seaboxdata.sdps.user.vo.user.PageApplyRequest;

import java.util.List;

public interface IUserApplyService extends ISuperService<UserApply> {

    /**
     * 生成申请表单信息
     * @param userApply
     */
    public void addUserApplyInfo(UserApply userApply);

    /**
     * 根据用户名查询申请表单信息(对应审批管理中我的申请功能)
     * @param userName
     * @return
     */
    public List<UserApplyDto> selectUserApplyInfo(String userName);


    /**
     * 审批管理-我的申请(筛选查询功能)
     * @return
     */
    public List<UserApplyDto> selectUserApplyInfoByWhere(String userName, String applyContent, Integer spStatus);

    /**
     * 审批管理-我的申请(筛选查询功能)
     * 分页查询
     * @return
     */
    public PageResult<UserApplyDto> selectUserApplyInfoPage(PageApplyRequest pageApplyRequest);


    /**
     * 更新申请表单信息
     */
    public void updateUserApplyInfo(ApplyVo applyVo);

    /**
     * 根据申请状态查询申请表单信息
     * @param spStatus
     * @return
     */
    public UserApply selectBySpStatus(String spStatus);

}
