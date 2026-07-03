package com.arch.policy.domain;

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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String policyNumber;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID quoteId;

    // Carried from rating as a string; policy-service does not own the ProductType enum.
    @Column(nullable = false)
    private String productType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal annualPremium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Policy() {
        // required by JPA
    }

    public Policy(String policyNumber, UUID customerId, UUID quoteId,
                  String productType, BigDecimal annualPremium, LocalDate effectiveDate) {
        this.policyNumber = policyNumber;
        this.customerId = customerId;
        this.quoteId = quoteId;
        this.productType = productType;
        this.annualPremium = annualPremium;
        this.status = PolicyStatus.ACTIVE;
        this.effectiveDate = effectiveDate;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getPolicyNumber() { return policyNumber; }
    public UUID getCustomerId() { return customerId; }
    public UUID getQuoteId() { return quoteId; }
    public String getProductType() { return productType; }
    public BigDecimal getAnnualPremium() { return annualPremium; }
    public PolicyStatus getStatus() { return status; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public Instant getCreatedAt() { return createdAt; }
}
