package com.seaboxdata.sdps.user.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.seaboxdata.sdps.common.core.service.impl.SuperServiceImpl;
import com.seaboxdata.sdps.user.api.IRoleGroupItemService;
import com.seaboxdata.sdps.user.mybatis.mapper.RoleGroupItemMapper;
import com.seaboxdata.sdps.user.mybatis.model.RoleGroupItem;

/**
 * @author 作者 owen E-mail: 624191343@qq.com
 */
@Slf4j
@Service
public class RoleGroupItemServiceImpl extends
		SuperServiceImpl<RoleGroupItemMapper, RoleGroupItem> implements
		IRoleGroupItemService {

}