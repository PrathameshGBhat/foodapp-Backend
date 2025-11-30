package com.cognizant.paymentservice.client;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrderClientConfiguration.class, FeignClientInterceptor.class})
public class CompositeFeignConfig {
}
