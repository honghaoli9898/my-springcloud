package com.seaboxdata.sdps.user.mybatis.mapper;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.user.mybatis.model.SdpsMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends SuperMapper<SdpsMessage>{

}
