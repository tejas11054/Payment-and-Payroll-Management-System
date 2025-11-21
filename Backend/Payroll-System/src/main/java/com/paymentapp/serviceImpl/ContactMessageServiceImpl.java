package com.paymentapp.serviceImpl;
 
import org.springframework.stereotype.Service;
import com.paymentapp.entity.ContactMessage;
import com.paymentapp.repository.ContactMessageRepository; 
import com.paymentapp.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
 
@Service 
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {
 
    private final ContactMessageRepository contactMessageRepository;
 
    @Override
    public ContactMessage saveMessage(ContactMessage message) {
        return contactMessageRepository.save(message);
    }
}