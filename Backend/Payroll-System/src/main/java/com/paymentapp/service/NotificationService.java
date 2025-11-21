package com.paymentapp.service;

import java.util.List;

import com.paymentapp.entity.Notification;
import com.paymentapp.entity.User;

public interface NotificationService {
    void sendEmail(String to, String subject, String body);
    // ✅ Create in-app notification
    void createInAppNotification(User user, String title, String message, 
                                 Long relatedId, String relatedEntityType, String priority);
    
    // ✅ Get user's in-app notifications
    List<Notification> getUserInAppNotifications(User user);
    
    // ✅ Get unread notifications
    List<Notification> getUnreadInAppNotifications(User user);
    
    // ✅ Get unread count
    Long getUnreadInAppNotificationCount(User user);
    
    // ✅ Mark as read
    void markInAppNotificationAsRead(Long notificationId);
    
    // ✅ Mark all as read
    void markAllInAppNotificationsAsRead(User user);
}
