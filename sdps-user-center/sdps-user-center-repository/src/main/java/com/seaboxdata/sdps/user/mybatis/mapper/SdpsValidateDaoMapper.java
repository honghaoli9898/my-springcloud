package com.seaboxdata.sdps.user.mybatis.mapper;

import java.util.List;

import com.seaboxdata.sdps.user.mybatis.model.ValidateDao;

public interface SdpsValidateDaoMapper {
	int insert(ValidateDao validateDao);

	ValidateDao selectByToken(String token);

	List<ValidateDao> selectByEmail(String email);
}
