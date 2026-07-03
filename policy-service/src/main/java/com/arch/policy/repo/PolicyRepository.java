package com.arch.policy.repo;

import com.arch.policy.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
}
