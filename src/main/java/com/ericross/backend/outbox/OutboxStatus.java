package com.ericross.backend.outbox;


public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
