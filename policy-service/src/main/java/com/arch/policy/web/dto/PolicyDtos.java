package com.arch.policy.web.dto;

import com.arch.policy.domain.PolicyStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class PolicyDtos {

    private PolicyDtos() {
    }

    public record BindPolicyRequest(
            @NotNull UUID customerId,
            @NotNull UUID quoteId
    ) {
    }

    public record PolicyResponse(
            UUID id,
            String policyNumber,
            UUID customerId,
            UUID quoteId,
            String productType,
            BigDecimal annualPremium,
            PolicyStatus status,
            LocalDate effectiveDate,
            Instant createdAt
    ) {
    }
}
