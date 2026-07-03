package com.arch.customer.service;

import com.arch.customer.domain.Customer;
import com.arch.customer.exception.DuplicateEmailException;
import com.arch.customer.exception.NotFoundException;
import com.arch.customer.repo.CustomerRepository;
import com.arch.customer.web.dto.CustomerDtos.CreateCustomerRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Customer create(CreateCustomerRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Customer already exists with email: " + request.email());
        }
        Customer customer = new Customer(
                request.firstName(), request.lastName(), request.email(), request.dateOfBirth());
        return repository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Customer> list() {
        return repository.findAll();
    }
}
