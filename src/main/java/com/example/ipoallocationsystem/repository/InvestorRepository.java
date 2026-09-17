package com.example.ipoallocationsystem.repository;

import com.example.ipoallocationsystem.entity.Investor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestorRepository extends JpaRepository<Investor, Long> {
    Optional<Investor> findByAccountKey(String accountKey);
}
