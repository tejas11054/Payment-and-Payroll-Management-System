package com.paymentapp.serviceImpl;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "application/pdf", "image/png", "image/jpeg", "image/jpg"
    );

    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024;

    public String uploadFile(MultipartFile file, Long orgId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File is too large. Max 10MB allowed");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        try {
            String publicId = "org_verifications/org_" + orgId + "_" + System.currentTimeMillis();

          
            Map<?,?> options = ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "auto",
                "overwrite", true
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new RuntimeException("Cloudinary upload failed: secure_url missing");
            }
            return secureUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }
}

