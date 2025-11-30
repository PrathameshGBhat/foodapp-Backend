package com.notificationservice.kafka;

import com.notificationservice.dto.OrdersDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationKafkaProducer {


	    private final KafkaTemplate<String, Object> kafkaTemplate;
	    private static final String TOPIC = "order_created_notifications";

	    public void sendOrderNotification(OrdersDto order) {
	        kafkaTemplate.send(TOPIC, order);
	        System.out.println("Sent order to Kafka: " + order.getOrderId());
	    }
}
