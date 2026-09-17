package com.example.ipoallocationsystem.controller;

import com.example.ipoallocationsystem.entity.Ipo;
import com.example.ipoallocationsystem.repository.IpoRepository;
import com.example.ipoallocationsystem.service.AllocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AllocationController {
    private final AllocationService allocationService;
    private final IpoRepository ipoRepository;

    public AllocationController(AllocationService allocationService, IpoRepository ipoRepository) {
        this.allocationService = allocationService;
        this.ipoRepository = ipoRepository;
    }

    @PostMapping("/ipos/{ipoId}/close")
    public Map<String, Object> closeSubscriptionWindow(@PathVariable Long ipoId){
        Optional<Ipo> ipo =  ipoRepository.findById(ipoId);
        if (ipo.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("detail", "invalid ipoId");
            return error;
        }
        ipo.get().setStatus(Ipo.IpoStatus.CLOSED);
        ipoRepository.save(ipo.get());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "closed");
        response.put("ipoId", ipo.get().getId());
        return response;
    }

    @PostMapping("/ipos/{ipoId}/allocate")
    public Map<String, Object> triggerAllocation(@PathVariable Long ipoId){
        return allocationService.calculateAllocation(ipoId);
    }

    @GetMapping("/ipos/{ipoId}/allocations")
    public List<Map<String, Object>> getAllocations(@PathVariable Long ipoId) {
        return allocationService.getAllocationResults(ipoId);
    }

    @GetMapping("/ipos/{ipoId}/reconcile")
    public Map<String, Object> reconcileAllocations(@PathVariable Long ipoId) {
        Optional<Ipo> ipoOpt = ipoRepository.findById(ipoId);
        if (ipoOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("detail", "invalid ipoId");
            return error;
        }
        Integer totalShares = ipoOpt.get().getTotalShares();

        List<Map<String, Object>> allocationResults = allocationService.getAllocationResults(ipoId);
        long totalAllocated = allocationResults.stream()
                .mapToLong(r -> (int) r.get("allocatedShares")).sum();

        Map<String, Object> response = new HashMap<>();
        response.put("totalShares", totalShares);
        response.put("totalAllocated", totalAllocated);
        response.put("unallocatedDueToRounding", totalShares - totalAllocated);
        response.put("isOverAllocated", totalAllocated > totalShares);
        return response;
    }


}
