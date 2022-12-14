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
            //true????????????????????????multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailConfig.getEmailFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            //System.out.println("html????????????????????????");
        } catch (Exception e) {
            System.out.println("html????????????????????????");
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
        //?????????
        String verCode = VerCodeGenerateUtil.getVerCode();
        //timestampToDate(new Date(), "yyyy-MM-dd hh:mm:ss");
        String time = DateUtil.now();

        Map<String, String> map = new HashMap<>();
        map.put("code", verCode);
        //String toEmail=StringUtils.join(to, ",");
        map.put("email", toEmail);

        //?????????????????????????????????session???
        session.setAttribute("verCode", map);
        Map<String, String> codeMap = (Map<String, String>) session.getAttribute("verCode");

        //??????????????????????????????????????????????????????
        /*try {
            scheduledExecutorService.schedule(new Thread(() -> {
                if (toEmail.equals(codeMap.get("email"))) {
                    session.removeAttribute("verCode");
                }
            }), 5 * 60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //??????????????? ;?????????????????????
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {

            //??????
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            //????????????
            helper.setSubject("??????????????????SDP??? ???????????????");

            //??????????????????????????????????????????
            String content = "<h3>\n" +
                    "\t<span style=\"font-size:16px;\">??????????????????</span> \n" +
                    "</h3>\n" +
                    "<p>\n" +
                    "\t<span style=\"font-size:14px;\">&nbsp;&nbsp;&nbsp;&nbsp;</span><span style=\"font-size:14px;\">&nbsp; <span style=\"font-size:16px;\">&nbsp;&nbsp;?????????????????????????????????????????????????????????????????????<span style=\"font-size:24px;color:#FFE500;\"> " + verCode + "</span>,????????????5????????????????????????5????????????????????????????????????????????????????????????????????????????????????????????????(????????????????????????????????????????????????????????????</span></span>\n" +
                    "</p>\n" +
                    "<p style=\"text-align:right;\">\n" +
                    "\t<span style=\"background-color:#FFFFFF;font-size:16px;color:#000000;\"><span style=\"color:#000000;font-size:16px;background-color:#FFFFFF;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;background-color:#FFFFFF;\">???????????????SDP</span></span></span> \n" +
                    "</p>\n" +
                    "<p style=\"text-align:right;\">\n" +
                    "\t<span style=\"background-color:#FFFFFF;font-size:14px;\"><span style=\"color:#FF9900;font-size:18px;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;\"><span style=\"font-size:16px;color:#000000;background-color:#FFFFFF;\">" + time + "</span><span style=\"font-size:18px;color:#000000;background-color:#FFFFFF;\"></span></span></span></span> \n" +
                    "</p>";
            //?????????????????????????????????html???????????????????????????ture???????????????html??????
            helper.setText(content, true);

            //?????????
            helper.setTo(toEmail);

            //test
            //?????????
            helper.setFrom(emailConfig.getEmailFrom());

            try {
                //????????????
                mailSender.send(mimeMessage);
            } catch (MailException e) {
                //???????????????????????????????????????
                return Result.failed("fail");
            }
        } catch (MessagingException e) {
            //????????????--???????????????
            return Result.failed("busy");
        }
        //?????????????????????
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
            //??????????????????????????????  ---???????????????
            return Result.failed("Invalid");
        }
        //??????????????? ????????? ?????????
        if (!verCode.toUpperCase().equals(code) || !toEmail.equals(email)) {
            return Result.failed("Captcha_Fail");
        }
        //?????????????????????session??????
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

        //?????? ?????????
        try {
            Result rs = codeMailCheck(toEmail, verCode);
            if (0 != rs.getCode()) {
                return Result.failed("vcode Invalid");
            }
        } catch (Exception e) {
            //??????????????????????????????  ---???????????????
            return Result.failed("Invalid");
        }

        //?????????????????????session??????
        request.getSession().removeAttribute("verCode");

        //add findUserByName

        return Result.succeed("chang pw success");
    }

    /*
     * ???????????????
     */
    public Result registerUser(String toEmail, String username,
                               String password,
                               String verCode, IUserService iUserService) {

        //????????????
        if(StrUtil.isBlank(toEmail)||StrUtil.isBlank(username)||StrUtil.isBlank(verCode)){
            return Result.failed("????????????????????????????????? ????????????");
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
            //??????????????????????????????  ---???????????????
            return Result.failed("Invalid");
        }

        //?????????????????????session??????
        request.getSession().removeAttribute("verCode");

        //add findUserByName and add user
        SysUser sysUser =iUserService.findUserByEmail(toEmail);
        SysUser sysUser2 =iUserService.selectByUsername(username);
        if(null != sysUser) {
            return Result.failed("?????????????????????????????????");
        }else if(null!=sysUser2){
            return Result.failed("??????????????????????????????");
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
                return Result.failed("???????????????");
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

        //??????????????????
        return Result.succeed("???????????????");
    }


}
