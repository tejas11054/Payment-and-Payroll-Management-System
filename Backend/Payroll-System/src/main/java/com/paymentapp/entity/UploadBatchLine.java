package com.paymentapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "upload_batch_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadBatchLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_batch_id", nullable = false)
    private UploadBatch uploadBatch;

    @Column(name = "line_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "raw_data", length = 2000)
    private String rawData;

    @Column(name = "status", length = 50, nullable = false)
    private String status; 

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;  

    @Column(name = "entity_id")
    private Long entityId; 
}
