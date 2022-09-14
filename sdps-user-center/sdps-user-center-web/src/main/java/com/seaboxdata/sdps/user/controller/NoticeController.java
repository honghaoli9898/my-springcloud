package com.seaboxdata.sdps.user.controller;


import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.user.mybatis.model.Notice;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.NoticeEmailVo;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.NoticeInterVo;
import com.seaboxdata.sdps.webssh.user.service.NoticeService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notice")
public class NoticeController{

    @Autowired
    private NoticeService noticeService;


    @PostMapping("/editNotice")
    public Result editNotice(@RequestBody NoticeInterVo notice) {
        Boolean res = noticeService.editNotice(notice);
        return Result.succeed(res);
    }

    @PostMapping("/editEmailNotice")
    public Result editEmailNotice(@RequestBody NoticeEmailVo notice) {
        Boolean res = noticeService.editEmailNotice(notice);
        return Result.succeed(res);
    }


    @PostMapping("/getNotice")
    public Result getNotice() {
        Notice res = noticeService.getNotice();
        return Result.succeed(res);
    }
}
