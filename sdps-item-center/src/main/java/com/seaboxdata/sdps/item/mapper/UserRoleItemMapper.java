package com.seaboxdata.sdps.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.model.UserRoleItem;

public interface UserRoleItemMapper extends SuperMapper<UserRoleItem> {
	public List<UserRoleItem> selectItemUserCnt(@Param("ids") List<Long> ids,@Param("isRank") boolean isRank);
}