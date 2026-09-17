package com.example.ipoallocationsystem.repository;

import com.example.ipoallocationsystem.entity.SubscriptionOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionOrderRepository extends JpaRepository<SubscriptionOrder, Long>{
    Optional<SubscriptionOrder> findByIpoIdAndInvestorId(Long ipoId, Long investorId);
    List<SubscriptionOrder> findAllByIpoId(Long ipoId);
}
