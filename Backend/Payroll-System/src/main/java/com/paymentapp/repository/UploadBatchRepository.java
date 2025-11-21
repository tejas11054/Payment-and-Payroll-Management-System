package com.paymentapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentapp.entity.UploadBatch;

public interface UploadBatchRepository extends JpaRepository<UploadBatch, Long> {}

