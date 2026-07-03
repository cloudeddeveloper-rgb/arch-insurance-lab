package com.arch.rating.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quotes")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal annualPremium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Quote() {
        // required by JPA
    }

    public Quote(UUID customerId, ProductType productType, BigDecimal coverageAmount,
                 BigDecimal annualPremium, Instant expiresAt) {
        this.customerId = customerId;
        this.productType = productType;
        this.coverageAmount = coverageAmount;
        this.annualPremium = annualPremium;
        this.status = QuoteStatus.QUOTED;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    /** Stored status is QUOTED at creation; a quote past its expiry reads as EXPIRED. */
    public QuoteStatus effectiveStatus() {
        if (status == QuoteStatus.QUOTED && Instant.now().isAfter(expiresAt)) {
            return QuoteStatus.EXPIRED;
        }
        return status;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public ProductType getProductType() { return productType; }
    public BigDecimal getCoverageAmount() { return coverageAmount; }
    public BigDecimal getAnnualPremium() { return annualPremium; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
