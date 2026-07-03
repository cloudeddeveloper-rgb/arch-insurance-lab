package com.arch.rating.repo;

import com.arch.rating.domain.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
}
