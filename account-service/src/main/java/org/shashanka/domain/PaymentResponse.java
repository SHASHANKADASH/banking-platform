package org.shashanka.domain;

import lombok.*;

@Builder
@Setter
@AllArgsConstructor
@ToString
@Getter
public class PaymentResponse {
    private Long paymentId;
    private String status;
    private Double remainingBalance;
}
