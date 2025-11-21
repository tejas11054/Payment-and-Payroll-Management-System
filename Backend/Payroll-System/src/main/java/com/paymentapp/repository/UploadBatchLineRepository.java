package com.paymentapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.paymentapp.entity.UploadBatchLine;

@Repository
public interface UploadBatchLineRepository extends JpaRepository<UploadBatchLine, Long> {
}
