package com.seaboxdata.sdps.licenseclient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 发布更新License通知
 */
@Component
public class RedisService{

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String LICENSE_KEY = "LICENSE_UPDATE";

    /**
     * 发布消息
     *
     * @param key
     * @param obj
     */
    public void release(String key, Object obj) {
        redisTemplate.convertAndSend(key, obj);
    }

    /**
     * 发布License更新通知
     *
     * @param obj
     */
    public void releaseLicenseUpdata(@Nullable Object obj) {
        release(LICENSE_KEY, obj);
    }
}
