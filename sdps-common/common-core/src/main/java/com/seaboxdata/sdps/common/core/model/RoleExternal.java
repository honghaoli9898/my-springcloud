package com.seaboxdata.sdps.common.core.model;

import com.baomidou.mybatisplus.annotation.TableName;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author pengosng
 * url 链接关系
 */
@Data
@TableName("sdps_role_external")
public class RoleExternal extends BasicObject{
    private Long id;
    @NotNull(message = "外部系统角色表主键不能为空")
    private Long externalId;
    @NotNull(message = "url主键不能为空")
    private Long urlId;
    @NotNull(message = "用户名称不能为空")
    private String username;
    private Integer delStatus;
    private Integer VerifyStatus;
    private String description;
}
