package com.seaboxdata.sdps.item.service.impl;

import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.item.mapper.UserRoleItemMapper;
import com.seaboxdata.sdps.item.model.UserRoleItem;
import com.seaboxdata.sdps.item.service.IUserRoleItemService;

@Service
public class UserRoleItemServiceImpl extends
		SuperServiceImpl<UserRoleItemMapper, UserRoleItem> implements
		IUserRoleItemService {
}