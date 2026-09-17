package com.example.ipoallocationsystem.controller;

import com.example.ipoallocationsystem.dto.SubscriptionOrderRequest;
import com.example.ipoallocationsystem.entity.Investor;
import com.example.ipoallocationsystem.repository.InvestorRepository;
import com.example.ipoallocationsystem.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final InvestorRepository investorRepository;

    public SubscriptionController(SubscriptionService subscriptionService, InvestorRepository investorRepository) {
        this.subscriptionService = subscriptionService;
        this.investorRepository = investorRepository;
    }

    @PostMapping("/subscriptions")
    public Map<String, Object> submitSubscription(
            @Valid @RequestBody SubscriptionOrderRequest request,
            @RequestHeader("X-Account-Key") String accountKey) {
        Optional<Investor> investorOpt = investorRepository.findByAccountKey(accountKey);
        if (investorOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("detail", "invalid account_key");
            return error;
        }
        return subscriptionService.submitSubscription(investorOpt.get().getId(), request);
    }
}
