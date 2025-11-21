package com.paymentapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.Notification;
import com.paymentapp.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	 // ✅ In-app notifications
    List<Notification> findByUserAndTypeOrderBySentAtDesc(User user, String type);
    
    List<Notification> findByUserAndTypeAndStatusOrderBySentAtDesc(User user, String type, String status);
    
    Long countByUserAndTypeAndStatus(User user, String type, String status);
    
    // ✅ Email/SMS notifications (existing)
    List<Notification> findByToEmailAndTypeOrderBySentAtDesc(String email, String type);
}
