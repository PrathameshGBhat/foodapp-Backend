package com.notificationservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.notificationservice.dto.VendorNotificationPayload;
import com.notificationservice.service.NotificationService;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order_placed", groupId = "notification-service",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consume(VendorNotificationPayload payload) {

        if (payload == null) {
            System.err.println("Received null payload!");
            return;
        }

        System.out.println("Received Notification Payload -> " + payload);

        if (payload.getVendorId() == null) {
            System.err.println("Invalid payload: vendorId is null");
            return;
        }

        notificationService.processOrderAndNotify(payload);
    }
}
