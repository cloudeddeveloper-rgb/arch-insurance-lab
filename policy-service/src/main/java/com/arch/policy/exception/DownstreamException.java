package com.arch.policy.exception;

/** A downstream service is unavailable or errored -> 502. */
public class DownstreamException extends RuntimeException {
    public DownstreamException(String message) {
        super(message);
    }
}
