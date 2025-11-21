package com.paymentapp.repository;
 
import org.springframework.data.jpa.repository.JpaRepository;
 
import com.paymentapp.entity.ContactMessage;
 
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long>{
 
}