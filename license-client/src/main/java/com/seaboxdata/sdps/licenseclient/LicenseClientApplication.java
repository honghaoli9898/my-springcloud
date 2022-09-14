package com.seaboxdata.sdps.licenseclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;

import com.seaboxdata.sdps.licenseclient.configuration.AppConfiguration;

//import org.springframework.cloud.sleuth.zipkin2.ZipkinAutoConfiguration;

//import zipkin2.codec.Encoding;
//import zipkin2.reporter.amqp.RabbitMQSender;

@SpringBootApplication
//EnableAutoConfiguration
@EnableDiscoveryClient
@RefreshScope
@EnableConfigurationProperties({AppConfiguration.class})
@EnableCircuitBreaker
@EnableCaching
@ComponentScan(basePackages = {"com.seaboxdata.sdps"})
public class LicenseClientApplication{


    public static void main(String[] args) {
        SpringApplication.run(LicenseClientApplication.class, args);
    }

	
	/*//按理是不需要这里创建的，springboot会自动创建，ZipkinRabbitSenderConfiguration，依赖的CachingConnectionFactory没有自动创建
	@Bean
	@Qualifier(ZipkinAutoConfiguration.SENDER_BEAN_NAME)
	@ConditionalOnMissingBean(name=ZipkinAutoConfiguration.SENDER_BEAN_NAME)
	public zipkin2.reporter.Sender getSender(){
		return RabbitMQSender.newBuilder().addresses(addresses + ":" + port).encoding(Encoding.JSON).password(password).
				username(username).virtualHost(virtualHost).build();
	}*/

}
