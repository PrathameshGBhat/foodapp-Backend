package com.notificationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.notificationservice.dto.OrdersDto;
import com.notificationservice.service.NotificationService;

@RestController
@RequestMapping("/api/notification")
@CrossOrigin // ONLY if frontend calls this directly
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody OrdersDto order) {

        // Basic validation (don't send garbage to service)
        if (order == null || order.getOrderId() == null || order.getRestaurantId() == null) {
            return ResponseEntity.badRequest().body("Invalid order payload");
        }

        notificationService.processOrderAndNotify(order);
        return ResponseEntity.ok("Notification forwarded to Kafka");
    }
}
