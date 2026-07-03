package com.arch.rating.web.dto;

import com.arch.rating.domain.ProductType;
import com.arch.rating.domain.QuoteStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class QuoteDtos {

    private QuoteDtos() {
    }

    public record CreateQuoteRequest(
            @NotNull UUID customerId,
            @NotNull ProductType productType,
            @NotNull @DecimalMin(value = "1000") BigDecimal coverageAmount
    ) {
    }

    public record QuoteResponse(
            UUID id,
            UUID customerId,
            ProductType productType,
            BigDecimal coverageAmount,
            BigDecimal annualPremium,
            QuoteStatus status,
            Instant expiresAt,
            Instant createdAt
    ) {
    }
}
