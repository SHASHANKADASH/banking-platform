package org.shashanka.domain;

import lombok.Builder;

@Builder
public class PaymentCompletedEvent {
    private Long paymentId;
    private Long accountId;
    private Double amount;
}
