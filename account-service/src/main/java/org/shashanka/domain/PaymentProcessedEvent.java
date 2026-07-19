package org.shashanka.domain;

public record PaymentProcessedEvent(
        Long paymentId,
        Long accountId,
        Double amount,
        String merchant,
        String status
) {}
