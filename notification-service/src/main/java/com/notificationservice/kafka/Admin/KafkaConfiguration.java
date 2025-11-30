package com.notificationservice.kafka.Admin;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {

	@Bean
	public NewTopic myTopic() {
		return TopicBuilder.name("order_placed").partitions(3).replicas(1).build();
	}

}
