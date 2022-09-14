package com.seaboxdata.sdps.licenseutil.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;

/**
 * @author cainiao
 */
@Configuration
public class LicenseConfig {
    @Bean
    CountDownLatch latch() {
        return new CountDownLatch(1);
    }
}
