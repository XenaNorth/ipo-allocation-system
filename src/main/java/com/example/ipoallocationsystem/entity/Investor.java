package com.example.ipoallocationsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "investors")
@Data
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String accountKey;
}
