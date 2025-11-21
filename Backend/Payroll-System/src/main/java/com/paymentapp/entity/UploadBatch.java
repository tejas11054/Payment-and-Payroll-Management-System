package com.paymentapp.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "upload_batch")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long batchId;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;  

    @Column(name = "file_url", length = 512, nullable = false)
    private String fileUrl;

    @Column(name = "record_count", nullable = false)
    private Integer recordCount;  

    @Column(name = "processed_count", nullable = false)
    private Integer processedCount; 

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(
        mappedBy = "uploadBatch",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<UploadBatchLine> lines = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "entity_type", length = 50)
    private String entityType;  

    public void addBatchLine(UploadBatchLine line) {
        lines.add(line);
        line.setUploadBatch(this);
    }

    public void removeBatchLine(UploadBatchLine line) {
        lines.remove(line);
        line.setUploadBatch(null);
    }
}
