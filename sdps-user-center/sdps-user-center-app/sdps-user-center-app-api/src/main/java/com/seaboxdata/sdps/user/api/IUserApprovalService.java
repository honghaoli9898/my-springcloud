package com.seaboxdata.sdps.user.api;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.model.UserApproval;
import com.seaboxdata.sdps.user.vo.user.ApprovalVo;
import com.seaboxdata.sdps.user.vo.user.PageApplyRequest;

import java.util.List;

public interface IUserApprovalService extends ISuperService<UserApproval> {

    /**
     * 根据用户名查询审批表单信息(对应审批管理中我的审批功能)
     * @param userName
     * @return
     */
    public List<UserApproval> selectUserApprovalInfo(String userName);

    /**
     * 审批管理-我的审批(筛选查询功能)
     * @param userName
     * @param status
     * @return
     */
    public List<UserApproval> selectUserApprovalInfoByWhere(String userName, Integer status, String applyContent, Integer spStatus);

    /**
     * 审批管理-我的审批(筛选查询功能)
     * 分页查询
     */
    public PageResult<UserApproval> selectUserApprovalInfoPage(PageApplyRequest pageApplyRequest);


    /**
     * 审批管理-我的审批(审批功能)
     */
    public void updateApprovalInfo(ApprovalVo approvalVo);


    /**
     * 新增审批
     * @param approvalVo
     * @param user
     * @return
     */
    Boolean addApproval(ApprovalVo approvalVo, SysUser user);

}
