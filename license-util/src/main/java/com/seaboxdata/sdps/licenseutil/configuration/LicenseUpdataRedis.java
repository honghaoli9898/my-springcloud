package com.seaboxdata.sdps.licenseutil.configuration;

import com.seaboxdata.sdps.licenseutil.event.LicenseUpdateListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;


import java.util.concurrent.CountDownLatch;

/**
 * License更新通知监听
 */
@Configuration
public class LicenseUpdataRedis {
    private static final String LICENSE_KEY = "LICENSE_UPDATE";
    private static final String METHOD_NAME = "licenseUpdateMessage";

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
//        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(
//                Object.class);
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        //接受消息的key
//        listenerAdapter.setSerializer(jackson2JsonRedisSerializer);
        container.addMessageListener(listenerAdapter, new PatternTopic(LICENSE_KEY));
        return container;
    }

    /**
     * 绑定消息监听者和接收监听的方法
     *
     * @param receiver
     * @return
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(LicenseUpdateListener receiver) {
        return new MessageListenerAdapter(receiver, METHOD_NAME);
    }

    //这里的latch2不能跟LicenseUtil模块中的latch同名否则启动时会bean创建失败
    @Bean
    LicenseUpdateListener receiver(CountDownLatch latch2) {
        return new LicenseUpdateListener(latch2);
    }

    //这里的latch2不能跟LicenseUtil模块中的latch同名否则启动时会bean创建失败
    @Bean
    CountDownLatch latch2() {
        return new CountDownLatch(1);
    }
}
