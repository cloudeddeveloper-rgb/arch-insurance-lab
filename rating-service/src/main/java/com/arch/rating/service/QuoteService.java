package com.arch.rating.service;

import com.arch.rating.domain.Quote;
import com.arch.rating.exception.NotFoundException;
import com.arch.rating.repo.QuoteRepository;
import com.arch.rating.web.dto.QuoteDtos.CreateQuoteRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class QuoteService {

    private static final Duration QUOTE_TTL = Duration.ofDays(30);

    private final QuoteRepository repository;
    private final RatingEngine ratingEngine;

    public QuoteService(QuoteRepository repository, RatingEngine ratingEngine) {
        this.repository = repository;
        this.ratingEngine = ratingEngine;
    }

    @Transactional
    public Quote create(CreateQuoteRequest request) {
        BigDecimal premium = ratingEngine.annualPremium(request.productType(), request.coverageAmount());
        Instant expiresAt = Instant.now().plus(QUOTE_TTL);
        Quote quote = new Quote(
                request.customerId(), request.productType(),
                request.coverageAmount(), premium, expiresAt);
        return repository.save(quote);
    }

    @Transactional(readOnly = true)
    public Quote getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quote not found: " + id));
    }
}
