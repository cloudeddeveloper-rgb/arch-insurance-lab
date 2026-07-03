package com.arch.rating.service;

import com.arch.rating.domain.ProductType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Deterministic, table-driven premium model. Real systems delegate to a rating
 * engine (Drools, RadarLive, Duck Creek). The point here is a pure, easily
 * testable calculation with correct money handling (BigDecimal, fixed scale).
 */
@Component
public class RatingEngine {

    public BigDecimal annualPremium(ProductType productType, BigDecimal coverageAmount) {
        BigDecimal rate = switch (productType) {
            case AUTO -> new BigDecimal("0.012");
            case HOME -> new BigDecimal("0.004");
            case UMBRELLA -> new BigDecimal("0.002");
        };
        BigDecimal flatFee = switch (productType) {
            case AUTO -> new BigDecimal("150");
            case HOME -> new BigDecimal("300");
            case UMBRELLA -> new BigDecimal("100");
        };
        return coverageAmount.multiply(rate).add(flatFee).setScale(2, RoundingMode.HALF_UP);
    }
}
