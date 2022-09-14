package com.seaboxdata.sdps.user.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;*/

import javafx.util.Pair;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.utils.VerCodeGenerateUtil;
import com.seaboxdata.sdps.user.api.ILoginForgetPwAndRegisterService;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.config.EmailConfig;
import com.seaboxdata.sdps.user.vo.user.CreateUserVo;

/**
 * @author tyt 2021-1124
 */
@Slf4j
@Service
public class LoginForgetPwAndRegisterServiceImpl implements ILoginForgetPwAndRegisterService {

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void sendSimpleMail(String[] to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(emailConfig.getEmailFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    @Override
    public void sendAttachmentsMail(String[] to, String subject, String content, List<Pair<String, File>> attachments) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailConfig.getEmailFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            if (attachments != null) {
                for (Pair<String, File> attachment : attachments) {
                    FileSystemResource file = new FileSystemResource(attachment.getValue());
                    helper.addAttachment(attachment.getKey(), file);
                }
            }
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            //true表示需要创建一个multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailConfig.getEmailFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            //System.out.println("html格式邮件发送成功");
        } catch (Exception e) {
            System.out.println("html格式邮件发送失败");
        }
    }

    /*send vcode*/
    public Result sendVcodeMail(String toEmail
            //, ScheduledExecutorService scheduledExecutorService
    ) {
        ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
                .cast(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = requestAttributes.getRequest();
        HttpSession session = request.getSession();
        //验证码
        String verCode = VerCodeGenerateUtil.getVerCode();
        //timestampToDate(new Date(), "yyyy-MM-dd hh:mm:ss");
        String time = DateUtil.now();

        Map<String, String> map = new HashMap<>();
        map.put("code", verCode);
        //String toEmail=StringUtils.join(to, ",");
        map.put("email", toEmail);

        //验证码和邮箱，一起放入session中
        session.setAttribute("verCode", map);
        Map<String, String> codeMap = (Map<String, String>) session.getAttribute("verCode");

        //创建计时线程池，到时间自动移除验证码
        /*try {
            scheduledExecutorService.schedule(new Thread(() -> {
                if (toEmail.equals(codeMap.get("email"))) {
                    session.removeAttribute("verCode");
                }
            }), 5 * 60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //发送邮件部 ;发送复杂的邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {

            //组装
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            //邮件标题
            helper.setSubject("【大数据平台SDP】 邮件验证码");

            //想要不一样的邮件格式，定制。
            String content = "<h3>\n" +
                    "\t<span style=\"font-size:16px;\">亲爱的用户：</span> \n" +
                    "</h3>\n" +
                    "<p>\n" +
                    "\t<span style=\"font-size:14px;\">&nbsp;&nbsp;&nbsp;&nbsp;</span><span style=\"font-size:14px;\">&nbsp; <span style=\"font-size:16px;\">&nbsp;&nbsp;您好！您正在进行邮箱验证，本次请求的验证码为：<span style=\"font-size:24px;color:#FFE500;\"> " + verCode + "</span>,本验证码5分钟内有效，请在5分钟内完成验证。（请勿泄露此验证码）如非本人操作，请忽略该邮件。(这是一封自动发送的邮件，请不要直接回复）</span></span>\n" +
                    "</p>\n" +
                    "<p style=\"text-align:right;\">\n" +
                    "\t<span style=\"background-color:#FFFFFF;font-size:16px;color:#000000;\"><span style=\"color:#000000;font-size:16px;background-color:#FFFFFF;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;background-color:#FFFFFF;\">大数据平台SDP</span></span></span> \n" +
                    "</p>\n" +
                    "<p style=\"text-align:right;\">\n" +
                    "\t<span style=\"background-color:#FFFFFF;font-size:14px;\"><span style=\"color:#FF9900;font-size:18px;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;\"><span style=\"font-size:16px;color:#000000;background-color:#FFFFFF;\">" + time + "</span><span style=\"font-size:18px;color:#000000;background-color:#FFFFFF;\"></span></span></span></span> \n" +
                    "</p>";
            //因为设置了邮件格式所以html标签有点多，后面的ture为支持识别html标签
            helper.setText(content, true);

            //收件人
            helper.setTo(toEmail);

            //test
            //发送方
            helper.setFrom(emailConfig.getEmailFrom());

            try {
                //发送邮件
                mailSender.send(mimeMessage);
            } catch (MailException e) {
                //邮箱是无效的，或者发送失败
                return Result.failed("fail");
            }
        } catch (MessagingException e) {
            //发送失败--服务器繁忙
            return Result.failed("busy");
        }
        //发送验证码成功
        return Result.succeed("success");

    }

    /*check vcode*/
    public Result codeMailCheck(String toEmail, String verCode) {

        ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
                .cast(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = requestAttributes.getRequest();
        HttpSession session = request.getSession();

        Map<String, String> codeMap = (Map<String, String>) session.getAttribute("verCode");
        String code = null;
        String email = null;
        try {
            code = codeMap.get("code");
            email = codeMap.get("email");
        } catch (Exception e) {
            //验证码过期，或未找到  ---验证码无效
            return Result.failed("Invalid");
        }
        //验证码判断 邮箱名 用户名
        if (!verCode.toUpperCase().equals(code) || !toEmail.equals(email)) {
            return Result.failed("Captcha_Fail");
        }
        //验证码使用完后session删除
        session.removeAttribute("verCode");
        return Result.succeed("check pass");
    }

    /*change pw*/
    public Result reSetPw(String toEmail, String username,
                          String password,
                          String verCode ) {
        ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
                .cast(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = requestAttributes.getRequest();

        //校验 验证码
        try {
            Result rs = codeMailCheck(toEmail, verCode);
            if (0 != rs.getCode()) {
                return Result.failed("vcode Invalid");
            }
        } catch (Exception e) {
            //验证码过期，或未找到  ---验证码无效
            return Result.failed("Invalid");
        }

        //验证码使用完后session删除
        request.getSession().removeAttribute("verCode");

        //add findUserByName

        return Result.succeed("chang pw success");
    }

    /*
     * 注册新用户
     */
    public Result registerUser(String toEmail, String username,
                               String password,
                               String verCode, IUserService iUserService) {

        //参数校验
        if(StrUtil.isBlank(toEmail)||StrUtil.isBlank(username)||StrUtil.isBlank(verCode)){
            return Result.failed("注册信息邮箱或参数为空 不可用！");
        }

        ServletRequestAttributes requestAttributes = ServletRequestAttributes.class
                .cast(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = requestAttributes.getRequest();

        try {
            Result rs = codeMailCheck(toEmail, verCode);
            if (0 != rs.getCode()) {
                return Result.failed("vcode Invalid");
            }
        } catch (Exception e) {
            //验证码过期，或未找到  ---验证码无效
            return Result.failed("Invalid");
        }

        //验证码使用完后session删除
        request.getSession().removeAttribute("verCode");

        //add findUserByName and add user
        SysUser sysUser =iUserService.findUserByEmail(toEmail);
        SysUser sysUser2 =iUserService.selectByUsername(username);
        if(null != sysUser) {
            return Result.failed("该用户邮箱已存在注册！");
        }else if(null!=sysUser2){
            return Result.failed("该用户名已存在注册！");
        } else {
/*
            SysUser sysUserNew= new SysUser();
            sysUserNew.setUsername(username);
            sysUserNew.setPassword(passwordEncoder.encode(password));
            sysUserNew.setNickname(username);
            sysUserNew.setEmail(toEmail);
            sysUserNew.setEnabled(true);

            try {
                iUserService.saveOrUpdateUser(sysUserNew);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.failed("注册失败！");
            }
            */
            CreateUserVo  registerUser=new CreateUserVo();
            registerUser.setUsername(username);
            registerUser.setNickname(username);
            registerUser.setPassword(passwordEncoder.encode(password));
            registerUser.setEmail(toEmail);
            List<Long> ids =new ArrayList<>();
            ids.add(0l);
            registerUser.setIds(ids);
            iUserService.createUserAllInfo(registerUser);
        }

        //返回注册成功
        return Result.succeed("注册成功！");
    }


}
