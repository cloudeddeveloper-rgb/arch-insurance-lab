package com.arch.policy.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Views over the other services' responses - only the fields policy-service needs.
 * Unknown JSON fields are ignored (Boot disables FAIL_ON_UNKNOWN_PROPERTIES),
 * so these can stay narrow even as the upstream responses grow.
 */
public final class DownstreamDtos {

    private DownstreamDtos() {
    }

    public record CustomerView(UUID id) {
    }

    public record QuoteView(
            UUID id,
            UUID customerId,
            String productType,
            BigDecimal annualPremium,
            String status
    ) {
    }
}
