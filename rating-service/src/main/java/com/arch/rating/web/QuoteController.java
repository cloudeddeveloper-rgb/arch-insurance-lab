package com.arch.rating.web;

import com.arch.rating.domain.Quote;
import com.arch.rating.service.QuoteService;
import com.arch.rating.web.dto.QuoteDtos.CreateQuoteRequest;
import com.arch.rating.web.dto.QuoteDtos.QuoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quotes")
@Tag(name = "Quotes", description = "Quote pricing")
public class QuoteController {

    private final QuoteService service;

    public QuoteController(QuoteService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create (price) a quote")
    public ResponseEntity<QuoteResponse> create(
            @Valid @RequestBody CreateQuoteRequest request,
            UriComponentsBuilder uriBuilder) {
        Quote saved = service.create(request);
        URI location = uriBuilder.path("/api/v1/quotes/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a quote by id")
    public QuoteResponse getById(@PathVariable UUID id) {
        return toResponse(service.getById(id));
    }

    private static QuoteResponse toResponse(Quote q) {
        return new QuoteResponse(
                q.getId(), q.getCustomerId(), q.getProductType(),
                q.getCoverageAmount(), q.getAnnualPremium(),
                q.effectiveStatus(), q.getExpiresAt(), q.getCreatedAt());
    }
}
