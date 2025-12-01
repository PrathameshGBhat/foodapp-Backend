package com.notificationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.notificationservice.dto.VendorNotificationPayload;
import com.notificationservice.service.NotificationService;

@RestController
@RequestMapping("/api/notification")
@CrossOrigin
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody VendorNotificationPayload payload) {

        if (payload == null || payload.getOrderId() == null || payload.getVendorId() == null) {
            return ResponseEntity.badRequest().body("Invalid notification payload");
        }

        notificationService.processOrderAndNotify(payload);

        return ResponseEntity.ok("Notification processed");
    }
}
