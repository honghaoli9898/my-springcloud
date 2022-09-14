package com.seaboxdata.sdps.webssh.user.service;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.user.mybatis.model.SdpsMessage;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.MessageVo;

public interface MessageService{

    PageResult<SdpsMessage> getAllMessage(PageRequest<MessageVo> messageVo);

    Boolean readMessage(MessageVo message);

    Boolean deleteMessage(MessageVo message);

    Integer getNotReadMessage(MessageVo message);
}
