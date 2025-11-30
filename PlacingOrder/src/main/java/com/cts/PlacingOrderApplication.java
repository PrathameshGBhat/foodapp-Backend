package com.cts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.cts.clients")
@EnableDiscoveryClient
@EnableMethodSecurity
public class PlacingOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlacingOrderApplication.class, args);
	}

}

//package com.cts;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.scheduling.annotation.EnableScheduling;
//
//@SpringBootApplication
//@EnableDiscoveryClient  // ✅ Register with Eureka
//@EnableKafka  // ✅ Enable Kafka
//@EnableFeignClients  // ✅ Enable Feign clients
//@EnableScheduling  // ✅ For scheduled tasks
//public class PlacingOrderApplication {
//    public static void main(String[] args) {
//        SpringApplication.run(PlacingOrderApplication.class, args);
//    }
//}