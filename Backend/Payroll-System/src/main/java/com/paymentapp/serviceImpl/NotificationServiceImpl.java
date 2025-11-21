package com.paymentapp.serviceImpl;

import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.entity.Notification;
import com.paymentapp.entity.User;
import com.paymentapp.repository.NotificationRepository;
import com.paymentapp.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    // ================= EXISTING EMAIL METHOD =================
    @Override
    @Transactional
    public void sendEmail(String to, String subject, String body) {
        Notification notification = Notification.builder()
                .toEmail(to)
                .subject(subject)
                .bodySummary(body)
                .type("EMAIL")
                .status("PENDING")
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            notification.setStatus("SENT");
        } catch (Exception e) {
            notification.setStatus("FAILED");
            notification.setBodySummary(body + "\nError: " + e.getMessage());
        }

        notificationRepository.save(notification);
    }

    // ================= IN-APP NOTIFICATION METHODS =================
    
    @Override
    @Transactional
    public void createInAppNotification(User user, String title, String message,
                                       Long relatedId, String relatedEntityType, String priority) {
        
        // ✅ CRITICAL: Validate user is not null
        if (user == null) {
            System.err.println("❌ Cannot create notification: User is null!");
            throw new IllegalArgumentException("User cannot be null for in-app notification");
        }
        
        System.out.println("🔔 Creating in-app notification for user: " + user.getEmail());
        System.out.println("   Title: " + title);
        System.out.println("   Related ID: " + relatedId);
        System.out.println("   Type: " + relatedEntityType);
        
        Notification notification = Notification.builder()
                .user(user) // ✅ This must not be null
                .toEmail(user.getEmail()) // Keep for reference
                .subject(title)
                .bodySummary(message)
                .type("IN_APP")
                .status("UNREAD")
                .relatedId(relatedId)
                .relatedEntityType(relatedEntityType)
                .priority(priority != null ? priority : "MEDIUM")
                .build();
        
        try {
            Notification saved = notificationRepository.save(notification);
            System.out.println("✅ Notification saved with ID: " + saved.getNotificationId());
            System.out.println("   User ID: " + saved.getUser().getUserId());
        } catch (Exception e) {
            System.err.println("❌ Failed to save notification: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public List<Notification> getUserInAppNotifications(User user) {
        System.out.println("📥 Fetching notifications for user: " + user.getEmail());
        List<Notification> notifications = notificationRepository.findByUserAndTypeOrderBySentAtDesc(user, "IN_APP");
        System.out.println("✅ Found " + notifications.size() + " notifications");
        return notifications;
    }
    
    @Override
    public List<Notification> getUnreadInAppNotifications(User user) {
        return notificationRepository.findByUserAndTypeAndStatusOrderBySentAtDesc(
            user, "IN_APP", "UNREAD"
        );
    }
    
    @Override
    public Long getUnreadInAppNotificationCount(User user) {
        Long count = notificationRepository.countByUserAndTypeAndStatus(user, "IN_APP", "UNREAD");
        System.out.println("🔢 Unread count for " + user.getEmail() + ": " + count);
        return count;
    }
    
    @Override
    @Transactional
    public void markInAppNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus("READ");
            notificationRepository.save(notification);
            System.out.println("✅ Notification marked as read: " + notificationId);
        });
    }
    
    @Override
    @Transactional
    public void markAllInAppNotificationsAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadInAppNotifications(user);
        unreadNotifications.forEach(notification -> notification.setStatus("READ"));
        notificationRepository.saveAll(unreadNotifications);
        System.out.println("✅ Marked all notifications as read for: " + user.getEmail());
    }
}
