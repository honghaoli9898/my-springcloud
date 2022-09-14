package com.seaboxdata.sdps.common.framework.bean.ranger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ranger用户实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VXUsers {
    /**
     * Ranger用户ID
     */
    private Integer id;
    /**
     * Ranger用户名
     */
    private String name;
    /**
     * Ranger用户名字
     */
    private String firstName;
    /**
     * Ranger用户姓氏
     */
    private String lastName;
    /**
     * 用户密码(至少8个字符且至少一个字母一个数字)
     */
    private String password;
    /**
     * 用户描述
     */
    private String description;
    /**
     * 用户状态
     */
    private Integer status;
    /**
     * 是否可见(0:不可见,1:可见)
     */
    private Integer isVisible;
    /**
     * 用户来源(0:内部用户,1:外部用户)
     */
    private Integer userSource;
    /**
     * 所属者(创建者)
     */
    private String owner;
    /**
     * 最后修改用户信息的用户
     */
    private String updatedBy;
    /**
     * 创建时间
     */
    private String createDate;
    /**
     * 更新时间
     */
    private String updateDate;
    /**
     * 用户的角色（ROLE_USER:普通用户,ROLE_ADMIN_AUDITOR:角色管理审计,ROLE_SYS_ADMIN:管理员）
     */
    private List<String> userRoleList;
    /**
     * 所属组ID
     */
    private List<Integer> groupIdList;
    /**
     * 所属组名
     */
    private List<String> groupNameList;

}
