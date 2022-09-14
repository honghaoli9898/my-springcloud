package com.seaboxdata.sdps.user.task;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.hutool.core.date.DateUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.constant.UrlConstants;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;
import com.seaboxdata.sdps.user.mybatis.mapper.MessageMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.NoticeMapper;
import com.seaboxdata.sdps.user.mybatis.model.Notice;
import com.seaboxdata.sdps.user.mybatis.model.SdpsMessage;
import com.seaboxdata.sdps.user.service.impl.ValidateService;

@Component
@Slf4j
public class ScheduleTask {

	@Autowired
	private ValidateService validateService;

	@Autowired
	private RedisRepository redisRepository;

	@Autowired
	private NoticeMapper noticeMapper;

	// 发送邮件的类
	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String from;

	private AtomicInteger count = new AtomicInteger(0);

	private AtomicInteger countMessage = new AtomicInteger(0);

	@Autowired
	private MessageMapper messageMapper;

	/**
	 * 邮件提醒 每天任务
	 */
	@Scheduled(cron = "0/30 00 10 ? * *")
	public void dayTask() {
		Date expireDate = (Date) redisRepository.get(CommonConstant.LICENSE);
		Date today = new Date();
		long diff = diff(expireDate, today);
		Notice notice = noticeMapper.selectOne(new QueryWrapper<>());
		String licenseDate = DateUtil.formatDateTime(expireDate);
		// 提醒时间 提前一个月
		if (Objects.equals(notice.getEmailTime(), CommonConstant.MONTH_TIME)
				&& diff <= 30) {
			// 一次性提醒
			sendEmailPre(notice, licenseDate);
			// 周期：天
			if (Objects.equals(CommonConstant.DAY_PERIOD,
					notice.getEmailPeriod())) {
				String emailUser = notice.getEmailUser();
				String[] split = emailUser
						.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendEmail(licenseDate, split[i]);
				}
			}
		} else if (Objects.equals(notice.getEmailTime(),
				CommonConstant.WEEK_TIME) && diff <= 7) {// 提前一周
			// 一次性提醒
			sendEmailPre(notice, licenseDate);
			// 周期：天
			if (Objects.equals(CommonConstant.DAY_PERIOD,
					notice.getEmailPeriod())) {
				String emailUser = notice.getEmailUser();
				String[] split = emailUser
						.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendEmail(licenseDate, split[i]);
				}
			}
		}
	}

	/**
	 * 邮件提醒 每周任务
	 */
	@Scheduled(cron = "0 00 20 ? * WED")
	public void weekTask() {
		Date expireDate = (Date) redisRepository.get(CommonConstant.LICENSE);
		Date today = new Date();
		long diff = diff(expireDate, today);
		Notice notice = noticeMapper.selectOne(new QueryWrapper<>());
		String licenseDate = DateUtil.formatDateTime(expireDate);
		// 提醒时间 提前一个月
		if (Objects.equals(notice.getEmailTime(), CommonConstant.MONTH_TIME)
				&& diff <= 30) {
			// 一次性提醒
			sendEmailPre(notice, licenseDate);
			// 周期：周
			if (Objects.equals(CommonConstant.WEEK_PERIOD,
					notice.getEmailPeriod())) {
				String emailUser = notice.getEmailUser();
				String[] split = emailUser
						.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendEmail(licenseDate, split[i]);
				}
			}
		} else if (Objects.equals(notice.getEmailTime(),
				CommonConstant.WEEK_TIME) && diff <= 7) {// 提前一周
			// 一次性提醒
			sendEmailPre(notice, licenseDate);
			// 周期：周
			if (Objects.equals(CommonConstant.WEEK_PERIOD,
					notice.getEmailPeriod())) {
				String emailUser = notice.getEmailUser();
				String[] split = emailUser
						.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendEmail(licenseDate, split[i]);
				}
			}
		}
	}

	/**
	 * 比较日期，并返回相隔天数
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long diff(Date date1, Date date2) {
		long diff = date1.getTime() - date2.getTime();
		long days = diff / (1000 * 60 * 60 * 24);
		return days;
	}

	/**
	 * 发送邮件
	 * 
	 * @param date
	 *            到期时间
	 * @param to
	 *            收件人
	 */
	public void sendEmail(String date, String to) {
		// 设置邮件内容
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		// multipart模式
		MimeMessageHelper mimeMessageHelper = null;
		try {
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,
					"utf-8");
			mimeMessageHelper.setTo(to);
			mimeMessageHelper.setFrom(from);
			mimeMessageHelper.setSubject("license到期提醒");
			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head>");
			sb.append("<body><h3>您的license即将到期，到期时间" + date
					+ " 为了不影响影响使用，请尽快处理</h3>" + "</body>");
			sb.append("</html>");
			// 启用html
			mimeMessageHelper.setText(sb.toString(), true);
			validateService.sendPasswordResetEmail(mimeMessage);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 发邮件之前的判断
	 * 
	 * @param notice
	 * @param licenseDate
	 */
	public void sendEmailPre(Notice notice, String licenseDate) {
		// 一次性提醒
		if (Objects.equals(CommonConstant.ONCE_PERIOD, notice.getEmailPeriod())) {
			int andGet = count.addAndGet(1);
			if (andGet < 2) {
				String emailUser = notice.getEmailUser();
				String[] split = emailUser
						.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendEmail(licenseDate, split[i]);
				}
			}
		}
	}

	/**
	 * 消息提醒 每天任务
	 */
	@Scheduled(cron = "0/30 00 10 ? * *")
	public void dayMessageTask() {
		Date expireDate = (Date) redisRepository.get(CommonConstant.LICENSE);
		Date today = new Date();
		long diff = diff(expireDate, today);
		Notice notice = noticeMapper.selectOne(new QueryWrapper<>());
		String licenseDate = DateUtil.formatDateTime(expireDate);

		// 提醒时间 提前一个月
		if (Objects.equals(notice.getInterTime(), CommonConstant.MONTH_TIME)
				&& diff <= 30) {
			// 一次性提醒
			sendMessagePre(notice, licenseDate);
			// 周期：天
			if (Objects.equals(CommonConstant.DAY_PERIOD,
					notice.getInterPeriod())) {
				String users = notice.getInterUser();
				String[] split = users.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendMessage(Long.parseLong(split[i]), licenseDate);
				}
			}
		} else if (Objects.equals(notice.getEmailTime(),
				CommonConstant.WEEK_TIME) && diff <= 7) {// 提前一周
			// 一次性提醒
			sendMessagePre(notice, licenseDate);
			// 周期：天
			if (Objects.equals(CommonConstant.DAY_PERIOD,
					notice.getInterPeriod())) {
				String users = notice.getInterUser();
				String[] split = users.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendMessage(Long.parseLong(split[i]), licenseDate);
				}
			}
		}
	}

	/**
	 * 消息提醒 每周任务
	 */
	@Scheduled(cron = "0 00 20 ? * WED")
	public void weekMessageTask() {
		Date expireDate = (Date) redisRepository.get(CommonConstant.LICENSE);
		Date today = new Date();
		long diff = diff(expireDate, today);
		String licenseDate = DateUtil.formatDateTime(expireDate);
		Notice notice = noticeMapper.selectOne(new QueryWrapper<>());

		// 提醒时间 提前一个月
		if (Objects.equals(notice.getInterTime(), CommonConstant.MONTH_TIME)
				&& diff <= 30) {
			// 一次性提醒
			sendMessagePre(notice, licenseDate);
			// 周期：周
			if (Objects.equals(CommonConstant.WEEK_PERIOD,
					notice.getInterPeriod())) {
				String users = notice.getInterUser();
				String[] split = users.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendMessage(Long.parseLong(split[i]), licenseDate);
				}
			}
		} else if (Objects.equals(notice.getEmailTime(),
				CommonConstant.WEEK_TIME) && diff <= 7) {// 提前一周
			// 一次性提醒
			sendMessagePre(notice, licenseDate);
			// 周期：周
			if (Objects.equals(CommonConstant.WEEK_PERIOD,
					notice.getInterPeriod())) {
				String users = notice.getInterUser();
				String[] split = users.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendMessage(Long.parseLong(split[i]), licenseDate);
				}
			}
		}
	}

	/**
	 * 发消息之前的判断
	 * 
	 * @param notice
	 * @param licenseDate
	 */
	public void sendMessagePre(Notice notice, String licenseDate) {
		// 一次性提醒
		if (Objects.equals(CommonConstant.ONCE_PERIOD, notice.getInterPeriod())) {
			int andGet = countMessage.addAndGet(1);
			if (andGet < 2) {
				String emailUser = notice.getInterUser();
				String[] split = emailUser
						.split(CommonConstant.HEADER_LABEL_SPLIT);
				for (int i = 0; i < split.length; i++) {
					sendMessage(Long.parseLong(split[i]), licenseDate);
				}
			}
		}
	}

	/**
	 * 推送消息
	 * 
	 * @param userId
	 * @param licenseDate
	 */
	public void sendMessage(Long userId, String licenseDate) {
		SdpsMessage message = new SdpsMessage();
		message.setIsDelete(UrlConstants.OK_STATUS);
		message.setMessageContent("【license到期提醒】您的license即将到期，到期时间"
				+ licenseDate + "为了不影响影响使用，请尽快处理");
		// 未读状态
		message.setIsAlreadyRead(UrlConstants.APPLYING_STATUS);
		message.setReceiverId(userId);
		message.setCreateTime(new Date());
		message.setSenderId(1L);
		message.setType(CommonConstant.LICENSE_TYPE);
		messageMapper.insert(message);
	}

	/**
	 * 刷新license
	 */
	@Scheduled(cron = "0/30 * * ? * *")
	public void refreshLicense() {
		Date expireDate = (Date) redisRepository.get(CommonConstant.LICENSE);
		Date today = new Date();
		long diff = diff(expireDate, today);
		if (diff < 0) {
			redisRepository.set(CommonConstant.LICENSE, expireDate);
		}
	}

}
