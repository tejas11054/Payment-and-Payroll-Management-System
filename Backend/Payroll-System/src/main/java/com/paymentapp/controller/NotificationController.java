package com.paymentapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.paymentapp.entity.Notification;
import com.paymentapp.entity.User;
import com.paymentapp.repository.UserRepository;
import com.paymentapp.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications() {
        User user = getCurrentUser();
        List<Notification> notifications = notificationService.getUserInAppNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        User user = getCurrentUser();
        List<Notification> notifications = notificationService.getUnreadInAppNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User user = getCurrentUser();
        Long count = notificationService.getUnreadInAppNotificationCount(user);
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markInAppNotificationAsRead(notificationId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marked as read");
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        User user = getCurrentUser();
        notificationService.markAllInAppNotificationsAsRead(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long notificationId) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification deleted");
        
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
