package com.paymentapp.serviceImpl;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.paymentapp.entity.Document;
import com.paymentapp.entity.Organization;
import com.paymentapp.repository.DocumentRepository;
import com.paymentapp.service.DocumentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

	private final Cloudinary cloudinary;
	private final DocumentRepository documentRepository;

	@Override
	public String uploadAndSaveDocument(MultipartFile file, String ownerType, Long ownerId, String docType,
			Long uploadedBy, Organization organization) {
		try {
			Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of());
			String cloudUrl = (String) uploadResult.get("secure_url");
			if (cloudUrl == null) {
				throw new RuntimeException("Upload failed, no URL returned");
			}

			Document document = Document.builder().ownerType(ownerType).ownerId(ownerId).cloudUrl(cloudUrl)
					.docType(docType).uploadedBy(uploadedBy).organization(organization).build();

			documentRepository.save(document);

			return cloudUrl;

		} catch (IOException e) {
			throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
		}
	}

	public void uploadAndSaveDocumentFromUrl(String fileUrl, String ownerType, Long ownerId, String docType,
			Long uploadedBy, Organization organization) {
		try {
			if (fileUrl == null || fileUrl.isBlank()) {
				throw new IllegalArgumentException("File URL is empty or null");
			}

			Document document = Document.builder().ownerType(ownerType).ownerId(ownerId).docType(docType)
					.uploadedBy(uploadedBy).organization(organization).cloudUrl(fileUrl) // ✅ Critical line
					.build();

			documentRepository.save(document);

		} catch (Exception e) {
			throw new RuntimeException("Error saving document from URL", e);
		}
	}

}
