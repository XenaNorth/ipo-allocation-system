package com.example.ipoallocationsystem.service.allocation;

import com.example.ipoallocationsystem.entity.SubscriptionOrder;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProRataAllocationStrategy implements AllocationStrategy {

    @Override
    public Map<Long, Integer> allocate(int totalShares, List<SubscriptionOrder> orders) {
        long totalRequested = orders.stream().mapToLong(SubscriptionOrder::getRequestedShares).sum();
        Map<Long, Integer> result = new HashMap<>();

        double ratio = totalRequested > 0 ? (double) totalShares / totalRequested : 0;

        if (ratio >= 1) {
            // not oversubscribed, allocate in full
            for (SubscriptionOrder o : orders) {
                result.put(o.getInvestorId(), o.getRequestedShares());
            }
        } else {
            // oversubscribed, allocate pro-rata, round down (never over-allocate)
            for (SubscriptionOrder o : orders) {
                result.put(o.getInvestorId(), (int) Math.floor(o.getRequestedShares() * ratio));
            }
        }
        return result;
    }
}
