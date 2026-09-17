package com.example.ipoallocationsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "allocation_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ipo_id", "investor_id"}))

@Data
public class AllocationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ipoId;
    private Long investorId;
    private Integer requestedShares;
    private Integer allocatedShares;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(unique = true, nullable = false)
    private String idempotencyKey;
}
