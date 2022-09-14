package com.seaboxdata.sdps.webssh.user.service;


import com.seaboxdata.sdps.user.mybatis.model.Notice;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.NoticeEmailVo;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.NoticeInterVo;

public interface NoticeService{

    Boolean editNotice(NoticeInterVo notice);

    Boolean editEmailNotice(NoticeEmailVo notice);

    Notice getNotice();
}
