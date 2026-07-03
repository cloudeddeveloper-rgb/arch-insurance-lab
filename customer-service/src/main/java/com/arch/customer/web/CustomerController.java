package com.arch.customer.web;

import com.arch.customer.domain.Customer;
import com.arch.customer.service.CustomerService;
import com.arch.customer.web.dto.CustomerDtos.CreateCustomerRequest;
import com.arch.customer.web.dto.CustomerDtos.CustomerResponse;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer records")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a customer")
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CreateCustomerRequest request,
            UriComponentsBuilder uriBuilder) {
        Customer saved = service.create(request);
        URI location = uriBuilder.path("/api/v1/customers/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by id")
    public CustomerResponse getById(@PathVariable UUID id) {
        return toResponse(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "List customers")
    public List<CustomerResponse> list() {
        return service.list().stream().map(CustomerController::toResponse).toList();
    }

    private static CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.getId(), c.getFirstName(), c.getLastName(),
                c.getEmail(), c.getDateOfBirth(), c.getCreatedAt());
    }
}
