package com.example.ipoallocationsystem.service;

import com.example.ipoallocationsystem.entity.AllocationResult;
import com.example.ipoallocationsystem.entity.Ipo;
import com.example.ipoallocationsystem.entity.SubscriptionOrder;
import com.example.ipoallocationsystem.repository.AllocationResultRepository;
import com.example.ipoallocationsystem.repository.IpoRepository;
import com.example.ipoallocationsystem.repository.SubscriptionOrderRepository;
import com.example.ipoallocationsystem.service.allocation.AllocationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AllocationService {

    private static final Logger log = LoggerFactory.getLogger(AllocationService.class);

    private final IpoRepository ipoRepository;
    private final SubscriptionOrderRepository orderRepository;
    private final AllocationResultRepository allocationRepository;
    private final AllocationStrategy allocationStrategy;

    public AllocationService(IpoRepository ipoRepository, SubscriptionOrderRepository orderRepository,
                             AllocationResultRepository allocationRepository,
                             AllocationStrategy allocationStrategy) {
        this.ipoRepository = ipoRepository;
        this.orderRepository = orderRepository;
        this.allocationRepository = allocationRepository;
        this.allocationStrategy = allocationStrategy;
    }

    @Transactional
    public Map<String, Object> calculateAllocation(Long ipoId) {
        log.info("Starting allocation calculation, ipoId={}", ipoId);
        Map<String, Object> response = new HashMap<>();
        Optional<Ipo> ipoOpt = ipoRepository.findById(ipoId);

        if (ipoOpt.isEmpty()) {
            response.put("status", "error");
            response.put("detail", "IPO not found");
            return response;
        }

        Ipo ipo = ipoOpt.get();

        if (ipo.getStatus() != Ipo.IpoStatus.CLOSED) {
            response.put("status", "error");
            response.put("detail", "Subscription window is not closed yet, cannot allocate");
            return response;
        }

        List<SubscriptionOrder> orders = orderRepository.findAllByIpoId(ipoId);
        long totalRequested = orders.stream().mapToLong(SubscriptionOrder::getRequestedShares).sum();

        double oversubscriptionRatio = ipo.getTotalShares() > 0
                ? (double) totalRequested / ipo.getTotalShares() : 0;

        Map<Long, Integer> allocationMap = allocationStrategy.allocate(ipo.getTotalShares(), orders);

        List<Map<String, Object>> results = new ArrayList<>();
        for (SubscriptionOrder order : orders) {
            int allocated = allocationMap.getOrDefault(order.getInvestorId(), 0);
            results.add(doAllocate(ipoId, order, allocated));
        }

        ipo.setStatus(Ipo.IpoStatus.ALLOCATED);
        ipoRepository.save(ipo);

        long totalAllocated = results.stream()
                .mapToLong(r -> (int) r.get("allocatedShares")).sum();

        response.put("status", "completed");
        response.put("totalShares", ipo.getTotalShares());
        response.put("totalRequested", totalRequested);
        response.put("oversubscriptionRatio", Math.round(oversubscriptionRatio * 100.0) / 100.0);
        response.put("totalAllocated", totalAllocated);
        response.put("detailsCount", results.size());

        log.info("Allocation calculation completed, ipoId={}, totalAllocated={}", ipoId, totalAllocated);
        return response;
    }

    private Map<String, Object> doAllocate(Long ipoId, SubscriptionOrder order, int allocatedShares) {
        Map<String, Object> result = new HashMap<>();
        String idempotencyKey = ipoId + "_" + order.getInvestorId() + "_allocation";

        Optional<AllocationResult> existing = allocationRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            result.put("investorId", order.getInvestorId());
            result.put("allocatedShares", existing.get().getAllocatedShares());
            result.put("status", "already_allocated");
            return result;
        }

        AllocationResult allocation = new AllocationResult();
        allocation.setIpoId(ipoId);
        allocation.setInvestorId(order.getInvestorId());
        allocation.setRequestedShares(order.getRequestedShares());
        allocation.setAllocatedShares(allocatedShares);
        allocation.setIdempotencyKey(idempotencyKey);

        try {
            allocationRepository.save(allocation);
            result.put("status", "allocated");
        } catch (DataIntegrityViolationException e) {
            result.put("status", "conflict_skipped");
        }

        result.put("investorId", order.getInvestorId());
        result.put("allocatedShares", allocatedShares);
        return result;
    }

    public List<Map<String, Object>> getAllocationResults(Long ipoId) {
        List<AllocationResult> allocations = allocationRepository.findAllByIpoId(ipoId);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (AllocationResult a : allocations) {
            Map<String, Object> item = new HashMap<>();
            item.put("investorId", a.getInvestorId());
            item.put("requestedShares", a.getRequestedShares());
            item.put("allocatedShares", a.getAllocatedShares());
            resultList.add(item);
        }
        return resultList;
    }
}
