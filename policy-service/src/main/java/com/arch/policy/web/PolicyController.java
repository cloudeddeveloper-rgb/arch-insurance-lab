package com.arch.policy.web;

import com.arch.policy.domain.Policy;
import com.arch.policy.service.PolicyService;
import com.arch.policy.web.dto.PolicyDtos.BindPolicyRequest;
import com.arch.policy.web.dto.PolicyDtos.PolicyResponse;
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
@RequestMapping("/api/v1/policies")
@Tag(name = "Policies", description = "Bind quotes into policies")
public class PolicyController {

    private final PolicyService service;

    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Bind a quote into a policy")
    public ResponseEntity<PolicyResponse> bind(
            @Valid @RequestBody BindPolicyRequest request,
            UriComponentsBuilder uriBuilder) {
        Policy saved = service.bind(request);
        URI location = uriBuilder.path("/api/v1/policies/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a policy by id")
    public PolicyResponse getById(@PathVariable UUID id) {
        return toResponse(service.getById(id));
    }

    private static PolicyResponse toResponse(Policy p) {
        return new PolicyResponse(
                p.getId(), p.getPolicyNumber(), p.getCustomerId(), p.getQuoteId(),
                p.getProductType(), p.getAnnualPremium(), p.getStatus(),
                p.getEffectiveDate(), p.getCreatedAt());
    }
}
