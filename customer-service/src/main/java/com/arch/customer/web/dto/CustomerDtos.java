package com.arch.customer.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Request/response DTOs. Records keep them immutable and boilerplate-free. */
public final class CustomerDtos {

    private CustomerDtos() {
    }

    public record CreateCustomerRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            @NotNull @Past LocalDate dateOfBirth
    ) {
    }

    public record CustomerResponse(
            UUID id,
            String firstName,
            String lastName,
            String email,
            LocalDate dateOfBirth,
            Instant createdAt
    ) {
    }
}
