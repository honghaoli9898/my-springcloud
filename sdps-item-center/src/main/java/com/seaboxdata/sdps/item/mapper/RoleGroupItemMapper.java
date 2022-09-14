package com.seaboxdata.sdps.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.model.RoleGroupItem;

public interface RoleGroupItemMapper extends SuperMapper<RoleGroupItem> {

	public List<ItemDto> selectRolesByIds(@Param("ids") List<Long> groupIds,
			@Param("itemId") Long itemId);
}