package com.paymentapp.controller;

import java.io.InputStream;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.paymentapp.dto.ChangePasswordDTO;
import com.paymentapp.dto.PaymentReceiptDTO;
import com.paymentapp.dto.VendorProfileDTO;
import com.paymentapp.dto.VendorRequestDTO;
import com.paymentapp.dto.VendorResponseDTO;
import com.paymentapp.security.CustomUserDetails;
import com.paymentapp.service.VendorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping(value = "/{orgId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VendorResponseDTO> createVendor(@Valid @PathVariable Long orgId,
                                                          @RequestPart("dto") VendorRequestDTO dto,
                                                          @RequestPart(value = "documentFile", required = false) MultipartFile documentFile,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {

        VendorResponseDTO response = vendorService.createVendor(orgId, dto, documentFile, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{orgId}/upload-vendors", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<VendorResponseDTO>> uploadVendors(@Valid @PathVariable Long orgId,
                                                                 @RequestParam("file") MultipartFile file,
                                                                 @RequestParam(value = "fileUrl", required = false) String fileUrl,
                                                                 @RequestParam(value = "documentFile", required = false) MultipartFile documentFile,
                                                                 @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must be provided");
        }

        InputStream is = file.getInputStream();
        String fname = file.getOriginalFilename();

        List<VendorResponseDTO> created = vendorService.createVendorsBulk(orgId, is, fname, fileUrl, documentFile, userDetails.getUser());

        return ResponseEntity.ok(created);
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<List<VendorResponseDTO>> listVendors(@Valid @PathVariable Long orgId) {
        return ResponseEntity.ok(vendorService.getVendorsByOrganization(orgId));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<VendorResponseDTO> getVendorById(@Valid @PathVariable Long vendorId) {
        return ResponseEntity.ok(vendorService.getVendorById(vendorId));
    }

    @PutMapping("/vendor/{vendorId}")
    public ResponseEntity<VendorResponseDTO> updateVendor(@Valid @PathVariable Long vendorId,
                                                          @RequestBody VendorRequestDTO dto,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(vendorService.updateVendor(vendorId, dto, userDetails.getUser()));
    }

    @DeleteMapping("/vendor/{vendorId}")
    public ResponseEntity<Void> deleteVendor(@Valid @PathVariable Long vendorId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        vendorService.deleteVendor(vendorId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/my-profile")
    public ResponseEntity<VendorProfileDTO> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        VendorProfileDTO profile = vendorService.getVendorProfileByUserId(userDetails.getUser().getUserId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/my-profile")
    public ResponseEntity<VendorProfileDTO> updateMyProfile(@Valid @RequestBody VendorProfileDTO profileDto,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        VendorProfileDTO updated = vendorService.updateVendorOwnProfile(userDetails.getUser().getUserId(), profileDto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/my-receipts")
    public ResponseEntity<List<PaymentReceiptDTO>> getMyReceipts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PaymentReceiptDTO> receipts = vendorService.getMyReceipts(userDetails.getUser().getUserId());
        return ResponseEntity.ok(receipts);
    }

    @GetMapping("/receipt/{receiptId}")
    public ResponseEntity<PaymentReceiptDTO> getReceiptDetails(@PathVariable Long receiptId,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentReceiptDTO receipt = vendorService.getReceiptDetails(receiptId, userDetails.getUser().getUserId());
        return ResponseEntity.ok(receipt);
    }

    @GetMapping("/receipt/{receiptId}/download")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long receiptId,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        byte[] receiptData = vendorService.downloadReceipt(receiptId, userDetails.getUser().getUserId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "receipt_" + receiptId + ".txt");
        
        return new ResponseEntity<>(receiptData, headers, HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordDTO dto,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        vendorService.changeVendorPassword(userDetails.getUser().getUserId(), dto);
        return ResponseEntity.ok("Password changed successfully");
    }
}
