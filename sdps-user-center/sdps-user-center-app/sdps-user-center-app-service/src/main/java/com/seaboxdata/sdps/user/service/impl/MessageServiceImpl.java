package com.seaboxdata.sdps.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.constant.UrlConstants;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Url;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.user.mybatis.mapper.MessageMapper;
import com.seaboxdata.sdps.user.mybatis.model.SdpsMessage;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.MessageVo;
import com.seaboxdata.sdps.webssh.user.service.MessageService;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

	@Autowired
	private MessageMapper messageMapper;

	@Override
	public PageResult<SdpsMessage> getAllMessage(
			PageRequest<MessageVo> messageVo) {
		QueryWrapper<SdpsMessage> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("receiver_id", messageVo.getParam().getUserId());
		queryWrapper.eq("is_delete", UrlConstants.OK_STATUS);
		if (StringUtils.isNotBlank(messageVo.getParam().getMessage())) {
			queryWrapper.like("message_content", messageVo.getParam()
					.getMessage());
		}
		if (Objects.nonNull(messageVo.getParam().getType())) {
			queryWrapper.eq("type", messageVo.getParam().getType());
		}
		if (Objects.nonNull(messageVo.getParam().getIsRead())) {
			queryWrapper
					.eq("is_already_read", messageVo.getParam().getIsRead());
		}
		PageHelper.startPage(messageVo.getPage(), messageVo.getSize());
		List<SdpsMessage> messages = messageMapper.selectList(queryWrapper);
		PageResult<SdpsMessage> success = (PageResult<SdpsMessage>) PageResult
				.success((long) messages.size(), messages);
		return success;
	}

	@Override
	public Boolean readMessage(MessageVo message) {
		List<Long> ids = message.getIds();
		try {
			ids.forEach(t -> {
				SdpsMessage sdpsMessage = messageMapper.selectById(t);
				sdpsMessage.setIsAlreadyRead(UrlConstants.OK_STATUS);
				messageMapper.updateById(sdpsMessage);
			});
			return true;
		} catch (Exception e) {
			log.error(e.toString());
			return false;
		}

	}

	@Override
	public Boolean deleteMessage(MessageVo message) {
		List<Long> ids = message.getIds();
		try {
			ids.forEach(t -> {
				SdpsMessage sdpsMessage = messageMapper.selectById(t);
				sdpsMessage.setIsDelete(UrlConstants.APPLYING_STATUS);
				messageMapper.updateById(sdpsMessage);
			});
			return true;
		} catch (Exception e) {
			log.error(e.toString());
			return false;
		}
	}

	@Override
	public Integer getNotReadMessage(MessageVo message) {
		QueryWrapper<SdpsMessage> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("receiver_id", message.getUserId());
		queryWrapper.eq("is_delete", UrlConstants.OK_STATUS);
		// 未读
		queryWrapper.eq("is_already_read", UrlConstants.APPLYING_STATUS);
		Long integer = messageMapper.selectCount(queryWrapper);
		return integer.intValue();
	}
}
