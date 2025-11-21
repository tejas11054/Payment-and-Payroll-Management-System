package com.paymentapp.service;

import java.util.List;

import com.paymentapp.dto.PaymentApprovalDTO;
import com.paymentapp.dto.PaymentReceiptDTO;
import com.paymentapp.dto.PaymentRequestDTO;
import com.paymentapp.entity.User;

public interface PaymentService {
    
    /**
     * Create new payment request
     */
    PaymentRequestDTO createRequest(PaymentRequestDTO requestDto);
    
    /**
     * Get all pending payment requests (for Bank Admin)
     */
    List<PaymentRequestDTO> getPendingRequests();
    
    /**
     * Get payment request by ID
     */
    PaymentRequestDTO getRequestById(Long paymentId);
    
    /**
     * Get all payment requests by organization
     */
    List<PaymentRequestDTO> getRequestsByOrg(Long orgId);
    
    /**
     * Approve payment request (Bank Admin)
     */
    PaymentRequestDTO approveRequest(Long paymentId, PaymentApprovalDTO approvalDto, User approver);
    
    /**
     * Reject payment request (Bank Admin)
     */
    PaymentRequestDTO rejectRequest(Long paymentId, PaymentApprovalDTO approvalDto, User approver);
    
    /**
     * Get all payment requests (for history/logs)
     */
    List<PaymentRequestDTO> getAllRequests();
    
    /**
     * Get payment receipt by payment ID
     */
    PaymentReceiptDTO getReceipt(Long paymentId);
    
    /**
     * Get all receipts by organization
     */
    List<PaymentReceiptDTO> getReceiptsByOrg(Long orgId);
}
