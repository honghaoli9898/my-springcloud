package com.seaboxdata.sdps.user.api;

import com.seaboxdata.sdps.common.core.model.Result;
import javafx.util.Pair;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public interface ILoginForgetPwAndRegisterService {
    /**
     * 发送简单邮件
     *
     * @param to      收件人地址
     * @param subject 邮件标题
     * @param content 邮件内容
     */
    public void sendSimpleMail(String[] to, String subject, String content);

    /**
     * 发送简单邮件
     *
     * @param to                 收件人地址
     * @param subject            邮件标题
     * @param content            邮件内容
     * @param attachments<文件名附件> 附件列表
     */
    public void sendAttachmentsMail(String[] to, String subject, String content, List<Pair<String, File>> attachments);


    /**
     * 发送html格式邮件
     *
     * @param subject 主题
     * @param content 内容
     */
    public void sendHtmlMail(String to, String subject, String content);

    /**
     * 发送vcode格式邮件
     *
     * @param to                       to邮件
     * //@param scheduledExecutorService 调度执行
     */
    public Result sendVcodeMail(String to);

    /**
     * 发送vcode格式邮件 check vcode
     *
     * @param toEmail to邮件
     * @param verCode 校验码
     */
    public Result codeMailCheck(String toEmail, String verCode);

    /**
     * 发送vcode格式邮件 changePw
     *
     * @param toEmail  to邮件
     * @param username 用户名
     * @param password 密码
     * @param verCode  校验码
     */
    public Result reSetPw(String toEmail, String username,
                          String password,
                          String verCode);

    /**
     * 发送vcode格式邮件 注册新用户
     *
     * @param toEmail  to邮件
     * @param username 用户名
     * @param password 密码
     * @param verCode  校验码
     */
    public Result registerUser(String toEmail, String username,
                               String password,
                               String verCode,IUserService iUserService);

}
