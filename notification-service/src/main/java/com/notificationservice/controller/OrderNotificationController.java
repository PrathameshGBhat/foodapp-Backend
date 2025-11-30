package com.notificationservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notificationservice.dto.OrdersDto;
import com.notificationservice.kafka.NotificationKafkaProducer;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class OrderNotificationController {

    private final NotificationKafkaProducer kafkaProducer;

    @PostMapping("/order-created")
    public String notifyOrderCreated(@RequestBody OrdersDto orderDto) {
        kafkaProducer.sendOrderNotification(orderDto);
        return "Order notification sent to Kafka";
    }
}
