package com.arch.policy.service;

import com.arch.policy.client.CustomerClient;
import com.arch.policy.client.RatingClient;
import com.arch.policy.client.dto.DownstreamDtos.QuoteView;
import com.arch.policy.domain.Policy;
import com.arch.policy.exception.NotFoundException;
import com.arch.policy.exception.UnprocessableException;
import com.arch.policy.repo.PolicyRepository;
import com.arch.policy.web.dto.PolicyDtos.BindPolicyRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepository repository;
    private final CustomerClient customerClient;
    private final RatingClient ratingClient;

    public PolicyService(PolicyRepository repository,
                         CustomerClient customerClient,
                         RatingClient ratingClient) {
        this.repository = repository;
        this.customerClient = customerClient;
        this.ratingClient = ratingClient;
    }

    @Transactional
    public Policy bind(BindPolicyRequest request) {
        // 1. Customer must exist (client turns a 404 into a 422).
        customerClient.getCustomer(request.customerId());

        // 2. Quote must exist, be bindable, and belong to this customer.
        QuoteView quote = ratingClient.getQuote(request.quoteId());
        if (!"QUOTED".equals(quote.status())) {
            throw new UnprocessableException(
                    "Quote is not bindable (status=" + quote.status() + "): " + request.quoteId());
        }
        if (!quote.customerId().equals(request.customerId())) {
            throw new UnprocessableException(
                    "Quote " + request.quoteId() + " does not belong to customer " + request.customerId());
        }

        // 3. Bind: premium and product are taken from the quote, not the caller.
        Policy policy = new Policy(
                newPolicyNumber(),
                request.customerId(),
                request.quoteId(),
                quote.productType(),
                quote.annualPremium(),
                LocalDate.now());
        return repository.save(policy);
    }

    @Transactional(readOnly = true)
    public Policy getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found: " + id));
    }

    private static String newPolicyNumber() {
        return "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
