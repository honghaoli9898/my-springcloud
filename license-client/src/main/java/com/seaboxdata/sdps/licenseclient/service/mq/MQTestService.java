package com.seaboxdata.sdps.licenseclient.service.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

/**
 * 队列发送和接收消息演示,定时任务
 *
 * @author stonehan
 */

@Service
@EnableScheduling
public class MQTestService{

    private static final Logger logger = LoggerFactory.getLogger(MQTestService.class);
	/*
	@Autowired
	@Qualifier("secondRabbitTemplate")
	RabbitTemplate rabbitTemplate;
	 
	//消费者
	 @RabbitListener(containerFactory="secondFactory",queues = "soso")
	public void process(org.springframework.amqp.core.Message message, Channel channel) throws IOException {
		 logger.info(new String(message.getBody(),"utf-8"));
		 channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
	 }
		
	//作为生产者
	 @Scheduled(fixedRate=300000)
	public void sendJob() {	
		rabbitTemplate.convertAndSend("soso","你好,rabbit");
	}

	 */
}
