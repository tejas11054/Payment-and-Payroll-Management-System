package com.paymentapp.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentapp.entity.UploadBatch;
import com.paymentapp.entity.UploadBatchLine;
import com.paymentapp.repository.UploadBatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadBatchService {

    private final UploadBatchRepository uploadBatchRepository;

    @Transactional
    public UploadBatch createBatchWithLines(UploadBatch batch, List<UploadBatchLine> lines) {
        lines.forEach(batch::addBatchLine);

        batch.setRecordCount(lines.size());
        batch.setProcessedCount(0);
        batch.setStatus("PENDING");

        return uploadBatchRepository.save(batch);
    }

    @Transactional
    public void processBatch(Long batchId) {
        UploadBatch batch = uploadBatchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Batch not found"));

        for (UploadBatchLine line : batch.getLines()) {
            if (line.getRawData().contains("duplicate email")) {
                line.setStatus("FAILED");
                line.setMessage("Duplicate email");
            } else {
                line.setStatus("SUCCESS");
                line.setMessage(null);
            }
        }

        batch.setProcessedCount(batch.getLines().size());
        batch.setStatus("COMPLETED");

        uploadBatchRepository.save(batch); 
    }
}
