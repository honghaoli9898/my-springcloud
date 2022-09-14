package com.seaboxdata.sdps.user.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.user.mybatis.mapper.NoticeMapper;
import com.seaboxdata.sdps.user.mybatis.model.Notice;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.NoticeEmailVo;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.NoticeInterVo;
import com.seaboxdata.sdps.webssh.user.service.NoticeService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService{

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public Boolean editNotice(NoticeInterVo notice) {
        try {
            Integer count = noticeMapper.selectCount(new QueryWrapper<>()).intValue();
            Notice real = new Notice();
            real.setInterPeriod(notice.getInterPeriod());
            real.setInterTime(notice.getInterTime());
            List<Long> users = notice.getInterUser();
            StringBuilder mails = new StringBuilder();
            if(null != users && users.size() > 0) {
                for(int i = 0; i < users.size(); i++) {
                    if(i == users.size() - 1){
                        mails.append(users.get(users.size() - 1));
                    }else {
                        mails.append(users.get(i) + CommonConstant.HEADER_LABEL_SPLIT);
                    }
                }
            }
            real.setInterUser(mails.toString());
            if(count <= 0){
                noticeMapper.insert(real);
            }else if (count >= 1) {
                noticeMapper.update(real,new QueryWrapper<>());
            }
        } catch (Exception e){
            log.error(e.toString());
            return false;
        }
        return true;
    }

    @Override
    public Boolean editEmailNotice(NoticeEmailVo notice) {
        try {
            Long count = noticeMapper.selectCount(new QueryWrapper<>());
            Notice real = new Notice();
            real.setEmailPeriod(notice.getEmailPeriod());
            real.setEmailTime(notice.getEmailTime());
            List<String> users = notice.getEmailUser();
            StringBuilder mails = new StringBuilder();
            if(null != users && users.size() > 0) {
                for(int i = 0; i < users.size(); i++) {
                    if(i == users.size() - 1){
                        mails.append(users.get(users.size() - 1));
                    }else {
                        mails.append(users.get(i) + CommonConstant.HEADER_LABEL_SPLIT);
                    }
                }
            }
            real.setEmailUser( mails.toString());
            if(count <= 0L){
                noticeMapper.insert(real);
            }else if (count >= 1) {
                noticeMapper.update(real,new QueryWrapper<>());
            }
        } catch (Exception e){
            log.error(e.toString());
            return false;
        }
        return true;
    }

    @Override
    public Notice getNotice() {
        Notice notice = noticeMapper.selectOne(new QueryWrapper<>());
        return notice;
    }
}
