package com.sdps.search.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.sdps.search.client.feign.fallback.SearchServiceFallbackFactory;
import com.sdps.search.client.service.impl.QueryServiceImpl;

/**
 * 控制是否加载搜索中心客户端的Service
 *
 * @author zlt
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
//@EnableFeignClients(basePackages = "com.central")
@Import({SearchServiceFallbackFactory.class, QueryServiceImpl.class})
public @interface EnableSearchClient {

}
