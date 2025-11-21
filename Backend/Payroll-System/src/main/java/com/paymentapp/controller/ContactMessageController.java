package com.paymentapp.controller;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.paymentapp.entity.ContactMessage;
import com.paymentapp.service.ContactMessageService;
 
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/public/contact")
@RequiredArgsConstructor
public class ContactMessageController {
 
    private final ContactMessageService contactMessageService;
 
    @PostMapping("/send")
    public ResponseEntity<String> receiveContactMessage(@RequestBody ContactMessage message) {
        contactMessageService.saveMessage(message);
        return ResponseEntity.ok("Message sent successfully. We will get back to you shortly.");
    }
    
    
}
 