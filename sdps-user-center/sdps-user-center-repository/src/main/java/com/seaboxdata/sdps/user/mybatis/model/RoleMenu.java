package com.seaboxdata.sdps.user.mybatis.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

/**
 * @author zlt
 * @date 2019/7/30
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("role_menu")
public class RoleMenu extends Model<RoleMenu> {
	private Long roleId;
	private Long menuId;
}
