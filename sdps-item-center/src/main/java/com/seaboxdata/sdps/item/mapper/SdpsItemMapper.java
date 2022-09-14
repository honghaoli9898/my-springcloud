package com.seaboxdata.sdps.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.SdpsItem;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.vo.item.ItemRequest;

public interface SdpsItemMapper extends SuperMapper<SdpsItem> {
	Page<ItemDto> findItemsByExample(@Param("item") ItemRequest item);

	List<ItemDto> findItemManagerByItemIds(@Param("ids") List<Long> ids,
			@Param("roleId") Long roleId);

	Page<ItemDto> findItemMembers(@Param("request") ItemRequest request);

	Page<ItemDto> findItemGroups(@Param("request") ItemRequest request);

	List<ItemDto> selectAllMembersByItemId(@Param("itemId") Long itemId);

	List<ItemDto> selectAllGroupByItemId(@Param("itemId") Long itemId);

	List<ItemDto> selectItemByUser(@Param("userId") Long userId,
			@Param("roleId") Long roleId);

	List<ItemDto> selectNotExisitMemberByItemId(@Param("itemId") Long itemId);

	List<ItemDto> selectNotExisitGroupByItemId(@Param("itemId") Long itemId);
	
	List<ItemDto> selectCurrentUserHasItemInfo(@Param("request")ItemRequest request);
}