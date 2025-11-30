package com.cognizant.paymentservice.client;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class OrderClientConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(OrderClientConfiguration.class);

    /**
     * Retry configuration for Feign client
     * Retries 3 times with exponential backoff
     */
    @Bean
    public Retryer retryer() {
        logger.info("Configuring Feign Retryer: 3 attempts with 100ms initial interval");
        return new Retryer.Default(100, 1000, 3);
    }

    /**
     * Timeout configuration
     */
    @Bean
    public Request.Options requestOptions() {
        logger.info("Configuring Feign Request Options: 5000ms connect, 5000ms read timeout");
        return new Request.Options(5000, 5000);
    }
}