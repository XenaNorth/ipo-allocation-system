package com.example.ipoallocationsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_orders",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ipo_id", "investor_id"}))

@Data
public class SubscriptionOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ipoId;
    private Long investorId;
    private Integer requestedShares;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(unique = true, nullable = false)
    private String submissionKey;

    @Version
    private Long version;

}
