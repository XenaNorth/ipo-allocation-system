package com.example.ipoallocationsystem.service.allocation;

import com.example.ipoallocationsystem.entity.SubscriptionOrder;
import java.util.List;
import java.util.Map;


public interface AllocationStrategy {
    /**
     * @param totalShares total shares issued by this IPO
     * @param orders      all subscription orders
     * @return key = investorId, value = shares allocated to that investor
     */
    Map<Long, Integer> allocate(int totalShares, List<SubscriptionOrder> orders);
}
