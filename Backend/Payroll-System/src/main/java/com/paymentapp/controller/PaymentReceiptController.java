package com.paymentapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.paymentapp.dto.PaymentReceiptDTO;
import com.paymentapp.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment-receipts")
@RequiredArgsConstructor
public class PaymentReceiptController {

    private final PaymentService paymentService;

    @GetMapping("/by-payment/{paymentId}")
    public ResponseEntity<PaymentReceiptDTO> getReceiptByPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getReceipt(paymentId));
    }

    @GetMapping("/org/{orgId}")
    public ResponseEntity<List<PaymentReceiptDTO>> getReceiptsForOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(paymentService.getReceiptsByOrg(orgId));
    }
}
