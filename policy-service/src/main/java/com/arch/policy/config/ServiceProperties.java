package com.arch.policy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Downstream base URLs, bound from the "services" prefix in application.yml.
 * Relaxed binding maps customer-base-url -> customerBaseUrl.
 */
@ConfigurationProperties(prefix = "services")
public record ServiceProperties(String customerBaseUrl, String ratingBaseUrl) {
}
