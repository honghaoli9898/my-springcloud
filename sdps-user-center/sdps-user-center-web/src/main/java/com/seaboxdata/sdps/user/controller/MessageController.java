package com.seaboxdata.sdps.user.controller;


import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.user.mybatis.model.SdpsMessage;
import com.seaboxdata.sdps.webssh.user.mybatis.vo.MessageVo;
import com.seaboxdata.sdps.webssh.user.service.MessageService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pengsong
 * 消息控制器
 */
@RestController
@RequestMapping("/message")
public class MessageController{

    @Autowired
    private MessageService messageService;

    @PostMapping("/getAllMessage")
    public PageResult getAllMessage(@RequestBody PageRequest<MessageVo> message) {
        PageResult<SdpsMessage> res = messageService.getAllMessage(message);
        return res;
    }

    @PostMapping("/readMessage")
    public Result readMessage(@RequestBody MessageVo message){
        Boolean res = messageService.readMessage(message);
        return Result.succeed(res);
    }

    @PostMapping("/deleteMessage")
    public Result deleteMessage(@RequestBody MessageVo message){
        Boolean res = messageService.deleteMessage(message);
        return Result.succeed(res);
    }

    @PostMapping("/getNotReadMessage")
    public Result getNotReadMessage(@RequestBody MessageVo message){
        Integer res = messageService.getNotReadMessage(message);
        return Result.succeed(res);
    }


}
