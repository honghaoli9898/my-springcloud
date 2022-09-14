package com.seaboxdata.sdps.user.mybatis.mapper;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.dto.user.UserApprovalDto;
import com.seaboxdata.sdps.user.mybatis.model.UserApproval;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserApprovalMapper extends SuperMapper<UserApproval> {

    // public void insertBatch(@Param("userApplyInfo") Collection<UserApply> userApplyInfo);


    /**
     * 更新审批表单信息
     * @param userName
     * @param applyContent
     * @param applyReason
     * @param applyTime
     */
    public void updateApprovalInfo(@Param("userName") String userName, @Param("applyContent") String applyContent,
                                   @Param("applyReason") String applyReason, @Param("applyTime") String applyTime);






    /**
     * 根据用户名查询审批表单信息
     * @param userName
     * @return
     */
    public List<UserApproval> selectInfoByUser(@Param("userName") String userName);

    /**
     * 条件查询
     * @param userName
     * @param status
     * @param applyContent
     * @param spStatus
     * @return
     */
    public List<UserApproval> selectInfoByWhere(@Param("userName") String userName, @Param("status") List<Integer> status,
                                                @Param("applyContent") String applyContent, @Param("spStatus") Integer spStatus);


    /**
     * 分页查询
     */
    public Page<UserApproval> selectInfoPage(@Param("userName") String userName,
                                             @Param("status") List<Integer> status,
                                             @Param("applyContent") String applyContent,
                                             @Param("spStatus") Integer spStatus);


}
