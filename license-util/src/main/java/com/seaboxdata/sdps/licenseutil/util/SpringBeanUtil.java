package com.seaboxdata.sdps.licenseutil.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 从spring容器中获取bean实例工具类
 * @author xiaoqiang
 * @date 2019/11/7 0:59
 */
@Component
public class SpringBeanUtil implements ApplicationContextAware
{
    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        SpringBeanUtil.applicationContext = applicationContext;
    }

    /**
     * 读取上下文
     * @return 上下文信息
     */
    public static ApplicationContext getContext(){
        return applicationContext;
    }

    /**
     * 根据名称从spring容器中获取bean实例
     * @param name bean名称
     * @param <T> 类型
     * @return bean
     */
    @SuppressWarnings("all")
    public static <T> T getBean(String name)
    {
        return (T) applicationContext.getBean(name);
    }

    /**
     * 根据类型从spring容器中获取bean实例
     * @param cls bean type
     * @param <T> 返回类型
     * @return bean
     */
    @SuppressWarnings("all")
    public static <T> T getBean(Class<?> cls) {
        return (T) applicationContext.getBean(cls);
    }
}
