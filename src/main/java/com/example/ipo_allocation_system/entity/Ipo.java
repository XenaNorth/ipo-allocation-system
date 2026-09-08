package com.example.ipo_allocation_system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ipos")
@Data
public class Ipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private Integer totalShares;
    private BigDecimal pricePerShare;
    private Integer maxSharesPerInvestor;
    private enum IpoStatus {
        OPEN, CLOSED, ALLOCATED;
    }
    @Enumerated(EnumType.STRING)
    private IpoStatus status = IpoStatus.OPEN;

    private LocalDateTime createdAt;
}
