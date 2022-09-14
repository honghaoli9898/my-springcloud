package com.seaboxdata.sdps.user.mybatis.mapper;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.dto.user.UserApplyDto;
import com.seaboxdata.sdps.user.mybatis.model.UserApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserApplyMapper extends SuperMapper<UserApply> {

    // public void insertBatch(@Param("userApplyInfo") Collection<UserApply> userApplyInfo);


    /**
     * 根据用户名查询申请表单信息
     * @param userName
     * @return
     */
    public List<UserApplyDto> selectInfoByUser(@Param("userName") String userName);


    /**
     * 条件查询（申请进度，申请内容，审批状态）
     */
    public List<UserApplyDto> selectInfoByWhere(@Param("userName") String userName,
                                                @Param("applyContent") String applyContent,
                                                @Param("spStatus") Integer spStatus);

    /**
     * 分页查询
     * @param userName
     * @param applyContent
     * @param spStatus
     * @return
     */
    public Page<UserApplyDto> selectInfoPage(@Param("userName") String userName,
                                                @Param("applyContent") String applyContent,
                                                @Param("spStatus") Integer spStatus);


    /**
     * 更新申请表单信息（再次申请）
     * @param userName
     * @param applyContent
     * @param applyReason
     * @param applyTime
     */
    public void updateApplyInfo(@Param("userName") String userName, @Param("applyContent") String applyContent,
                                @Param("applyReason") String applyReason, @Param("applyTime") String applyTime);

    /**
     * 根据申请信息查询申请记录信息
     * @param userName
     * @param applyContent
     * @return
     */
    public UserApplyDto selectInfoByApply(@Param("userName") String userName,
                                          @Param("applyContent") String applyContent);

//    public List<UserApply> selectBySqStatus(@Param("spStatus") Integer spStatus);

}
