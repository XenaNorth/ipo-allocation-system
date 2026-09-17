package com.example.ipoallocationsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SubscriptionOrderRequest {

    @NotNull
    private Long ipoId;

    @NotNull
    @Positive(message = "Requested shares must be positive")
    private Integer requestedShares;

    @NotNull
    private String submissionKey;
}
