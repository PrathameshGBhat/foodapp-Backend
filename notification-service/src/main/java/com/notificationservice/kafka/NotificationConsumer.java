package com.notificationservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationservice.dto.OrdersDto;
import com.notificationservice.service.NotificationService;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper mapper;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(topics = "order_placed", groupId = "notification-service")
    public void consume(String message) {
        System.out.println("Received Kafka message -> " + message);
        try {
            OrdersDto order = mapper.readValue(message, OrdersDto.class);

            if (order == null) {
                System.err.println("Parsed order is null");
                return;
            }

            if (order.getRestaurantId() == null) {
                System.err.println("Invalid order: restaurantId is null");
                return;
            }

            notificationService.processOrderAndNotify(order);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
