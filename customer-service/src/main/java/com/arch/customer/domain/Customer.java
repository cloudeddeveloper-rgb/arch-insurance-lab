package com.arch.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private @Getter UUID id;

    @Column(nullable = false)
    private @Getter String firstName;

    @Column(nullable = false)
    private @Getter String lastName;

    @Column(nullable = false, unique = true)
    private @Getter String email;

    @Column(nullable = false)
    private @Getter LocalDate dateOfBirth;

    @Column(nullable = false, updatable = false)
    private @Getter Instant createdAt;

    public Customer(String firstName, String lastName, String email, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
