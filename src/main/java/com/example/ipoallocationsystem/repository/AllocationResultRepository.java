package com.example.ipoallocationsystem.repository;

import com.example.ipoallocationsystem.entity.AllocationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AllocationResultRepository extends JpaRepository<AllocationResult, Long> {
    List<AllocationResult> findAllByIpoId(Long ipoId);
    Optional<AllocationResult> findByIdempotencyKey(String idempotencyKey);
}
