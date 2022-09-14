package com.seaboxdata.sdps.licenseclient.configuration;

import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * @author coffysun@tencent.com
 * @since 2020-07-27 21:28
 */
@Configuration
public class DruidConfig{

    @Bean
    public WallFilter wallFilter() {
        WallFilter wallFilter = new WallFilter();
        wallFilter.setConfig(wallConfig());
        return wallFilter;
    }

    @Bean
    public WallConfig wallConfig() {
        WallConfig config = new WallConfig();
        //允许一次执行多条语句
        config.setMultiStatementAllow(true);
        //允许非基本语句的其他语句
        config.setNoneBaseStatementAllow(true);
        //允许SQL中存在注释
        config.setCommentAllow(true);
        return config;
    }
}
