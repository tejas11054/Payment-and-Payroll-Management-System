package com.paymentapp.service;

import org.springframework.web.multipart.MultipartFile;

import com.paymentapp.entity.Organization;

public interface DocumentService {
	public String uploadAndSaveDocument(MultipartFile file, String ownerType, Long ownerId, String docType,
			Long uploadedBy, Organization organization);

	public void uploadAndSaveDocumentFromUrl(String fileUrl, String ownerType, Long ownerId, String docType,
			Long uploadedBy, Organization organization) ;
}


