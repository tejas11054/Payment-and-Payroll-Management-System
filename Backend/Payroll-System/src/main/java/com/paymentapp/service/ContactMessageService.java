package com.paymentapp.service;
 
import com.paymentapp.entity.ContactMessage;
 
public interface ContactMessageService {
	
	ContactMessage saveMessage(ContactMessage message);
}