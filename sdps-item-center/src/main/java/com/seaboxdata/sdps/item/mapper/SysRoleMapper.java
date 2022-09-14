package com.seaboxdata.sdps.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.core.model.SysRole;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.dto.item.ItemDto;

public interface SysRoleMapper extends SuperMapper<SysRole> {
	public List<ItemDto> selectRolesByIds(@Param("ids") List<Long> ids,
			@Param("itemId") Long itemId);
}