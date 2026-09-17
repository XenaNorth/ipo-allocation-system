package com.example.ipoallocationsystem.service;

import com.example.ipoallocationsystem.dto.SubscriptionOrderRequest;
import com.example.ipoallocationsystem.entity.Ipo;
import com.example.ipoallocationsystem.entity.SubscriptionOrder;
import com.example.ipoallocationsystem.repository.IpoRepository;
import com.example.ipoallocationsystem.repository.SubscriptionOrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final IpoRepository ipoRepository;
    private final SubscriptionOrderRepository orderRepository;

    public SubscriptionService(IpoRepository ipoRepository, SubscriptionOrderRepository orderRepository) {
        this.ipoRepository = ipoRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Map<String, Object> submitSubscription(Long investorId, SubscriptionOrderRequest request) {
        Map<String, Object> response = new HashMap<>();

        Optional<Ipo> ipoOpt = ipoRepository.findById(request.getIpoId());
        if (ipoOpt.isEmpty()) {
            response.put("status", "error");
            response.put("detail", "IPO not found");
            return response;
        }
        Ipo ipo = ipoOpt.get();

        if (ipo.getStatus() != Ipo.IpoStatus.OPEN) {
            response.put("status", "error");
            response.put("detail", "Subscription window is closed");
            return response;
        }

        if (ipo.getMaxSharesPerInvestor() != null && request.getRequestedShares() > ipo.getMaxSharesPerInvestor()) {
            response.put("status", "error");
            response.put("detail", "Exceeds max shares per investor (" + ipo.getMaxSharesPerInvestor() + ")");
            return response;
        }

        // idempotency check
        Optional<SubscriptionOrder> existing = orderRepository.findByIpoIdAndInvestorId(request.getIpoId(), investorId);
        if (existing.isPresent()) {
            response.put("status", "duplicate_ignored");
            response.put("orderId", existing.get().getId());
            return response;
        }

        SubscriptionOrder order = new SubscriptionOrder();
        order.setIpoId(request.getIpoId());
        order.setInvestorId(investorId);
        order.setRequestedShares(request.getRequestedShares());
        order.setSubmissionKey(request.getSubmissionKey());

        try {
            orderRepository.save(order);
            response.put("status", "submitted");
            response.put("orderId", order.getId());
        } catch (DataIntegrityViolationException e) {
            // unique constraint conflict (e.g. duplicate submissionKey) means duplicate request
            response.put("status", "duplicate_ignored");
        }

        return response;
    }
}
