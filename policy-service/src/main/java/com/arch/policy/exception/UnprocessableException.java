package com.arch.policy.exception;

/** A downstream resource is missing or a business rule fails -> 422. */
public class UnprocessableException extends RuntimeException {
    public UnprocessableException(String message) {
        super(message);
    }
}
