package com.cognizant.logging.config;

import com.cognizant.logging.filter.RequestLoggingFilter;
import com.cognizant.logging.filter.ResponseLoggingFilter;
import com.cognizant.logging.util.LoggingUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class LoggingAutoConfiguration {

    @Bean
    public RequestLoggingFilter requestLoggingFilter(LoggingUtil loggingUtil) {
        return new RequestLoggingFilter(loggingUtil);
    }

    @Bean
    public ResponseLoggingFilter responseLoggingFilter(LoggingUtil loggingUtil) {
        return new ResponseLoggingFilter(loggingUtil);
    }
}
