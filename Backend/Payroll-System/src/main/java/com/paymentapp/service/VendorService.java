package com.paymentapp.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.dto.ChangePasswordDTO;
import com.paymentapp.dto.PaymentReceiptDTO;
import com.paymentapp.dto.VendorProfileDTO;
import com.paymentapp.dto.VendorRequestDTO;
import com.paymentapp.dto.VendorResponseDTO;
import com.paymentapp.entity.PaymentRequest;
import com.paymentapp.entity.User;

public interface VendorService {

    // ✅ EXISTING METHODS
    void addBalanceToVendor(Long vendorId, BigDecimal amount, User performingUser);
    
    PaymentRequest initiateVendorPayment(Long orgId, Long vendorId, BigDecimal amount, String invoiceRef, User requestedBy);
    
    String processVendorPaymentRequest(Long paymentId, String action, String comment, User bankAdmin);
    
    VendorResponseDTO createVendor(Long orgId, VendorRequestDTO dto, MultipartFile documentFile, User performingUser);
    
    List<VendorResponseDTO> createVendorsBulk(Long orgId, InputStream fileInputStream, String fileName, String fileUrl,
            MultipartFile documentFile, User performingUser) throws Exception;
    
    List<VendorResponseDTO> getVendorsByOrganization(Long orgId);
    
    VendorResponseDTO getVendorById(Long vendorId);
    
    VendorResponseDTO updateVendor(Long vendorId, VendorRequestDTO dto, User performingUser);
    
    void deleteVendor(Long vendorId, User performingUser);

    // ✅ NEW METHODS FOR VENDOR DASHBOARD
    
    /**
     * Get vendor profile by user ID (logged in vendor)
     */
    VendorProfileDTO getVendorProfileByUserId(Long userId);
    
    /**
     * Update vendor own profile
     */
    VendorProfileDTO updateVendorOwnProfile(Long userId, VendorProfileDTO profileDto);
    
    /**
     * Get payment receipts for logged in vendor
     */
    List<PaymentReceiptDTO> getMyReceipts(Long userId);
    
    /**
     * Get single receipt details
     */
    PaymentReceiptDTO getReceiptDetails(Long receiptId, Long userId);
    
    /**
     * Download receipt as text/PDF
     */
    byte[] downloadReceipt(Long receiptId, Long userId);
    
    /**
     * Change vendor password
     */
    void changeVendorPassword(Long userId, ChangePasswordDTO dto);
}
