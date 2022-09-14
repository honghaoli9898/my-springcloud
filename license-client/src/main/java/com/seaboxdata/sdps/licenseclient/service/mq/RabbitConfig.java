package com.seaboxdata.sdps.licenseclient.service.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * 如果mq的配置有问题，不会影响启动，但是启动日志里会不停打印错误信息，可以不用理会
 *
 * 作为消费者
 *
 * @author stonehan
 * @RabbitListener(containerFactory="firstFactory",queues = "你的队列名字") public
 *         void
 *         process(org.springframework.amqp.core.Message
 *         message, Channel
 *         channel) throws
 *         IOException {}
 *
 *         作为生产者
 * @Autowired @Qualifier("secondRabbitTemplate") RabbitTemplate rabbitTemplate;
 *         rabbitTemplate.convertAndSend("soso",obj);
 */

@Configuration
public class RabbitConfig{

    private static final Logger logger = LoggerFactory.getLogger(RabbitConfig.class);
/*
	@Primary
	@Bean(name = "firstConnectionFactory")
	@ConditionalOnProperty({ "spring.rabbitmq.first.host", "spring.rabbitmq.first.port" })
	public ConnectionFactory sxmlConnectionFactorySingle(@Value("${spring.rabbitmq.first.host}") String host,
			@Value("${spring.rabbitmq.first.port}") int port,
			@Value("${spring.rabbitmq.first.username}") String username,
			@Value("${spring.rabbitmq.first.password}") String password,
			@Value("${spring.rabbitmq.first.virtual-host}") String virtualHost) {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(host);
		connectionFactory.setPort(port);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		return connectionFactory;
	}

	@Primary
	@Bean(name = "firstConnectionFactory")
	@ConditionalOnProperty("spring.rabbitmq.first.addresses")
	public ConnectionFactory sxmlConnectionFactoryCluster(@Value("${spring.rabbitmq.first.host}") String host,
			@Value("${spring.rabbitmq.first.addresses}") String addresses,
			@Value("${spring.rabbitmq.first.username}") String username,
			@Value("${spring.rabbitmq.first.password}") String password,
			@Value("${spring.rabbitmq.first.virtual-host}") String virtualHost) {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setAddresses(addresses);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		return connectionFactory;
	}

	@Bean(name = "firstRabbitTemplate")
	public RabbitTemplate sxmlRabbitTemplate(@Qualifier("firstConnectionFactory") ConnectionFactory connectionFactory) {
		RabbitTemplate firstRabbitTemplate = new RabbitTemplate(connectionFactory);
		return firstRabbitTemplate;
	}

	@Bean(name = "firstFactory")
	public SimpleRabbitListenerContainerFactory sxmlFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
			@Qualifier("firstConnectionFactory") ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		configurer.configure(factory, connectionFactory);
		// TODO: 注意这里写死的，实际情况根据需要修改
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		return factory;
	}

	@Bean(name = "secondConnectionFactory")
	@ConditionalOnProperty({ "spring.rabbitmq.second.host", "spring.rabbitmq.second.port" })
	public ConnectionFactory secondConnectionFactorySingle(@Value("${spring.rabbitmq.second.host}") String host,
			@Value("${spring.rabbitmq.second.port}") int port,
			@Value("${spring.rabbitmq.second.username}") String username,
			@Value("${spring.rabbitmq.second.password}") String password,
			@Value("${spring.rabbitmq.second.virtual-host}") String virtualHost) {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(host);
		connectionFactory.setPort(port);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		return connectionFactory;
	}

	@Bean(name = "secondConnectionFactory")
	@ConditionalOnProperty({ "spring.rabbitmq.second.addresses" })
	public ConnectionFactory secondConnectionFactoryCluster(
			@Value("${spring.rabbitmq.second.addresses}") String addresses,
			@Value("${spring.rabbitmq.second.username}") String username,
			@Value("${spring.rabbitmq.second.password}") String password,
			@Value("${spring.rabbitmq.second.virtual-host}") String virtualHost) {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setAddresses(addresses);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		return connectionFactory;
	}

	@Bean(name = "secondRabbitTemplate")
	public RabbitTemplate secondRabbitTemplate(
			@Qualifier("secondConnectionFactory") ConnectionFactory connectionFactory) {
		RabbitTemplate secondRabbitTemplate = new RabbitTemplate(connectionFactory);
		return secondRabbitTemplate;
	}

	@Bean(name = "secondFactory")
	public SimpleRabbitListenerContainerFactory secondFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
			@Qualifier("secondConnectionFactory") ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		// TODO: 注意这里写死的，实际情况根据需要修改
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		configurer.configure(factory, connectionFactory);
		return factory;
	}

 */

}
