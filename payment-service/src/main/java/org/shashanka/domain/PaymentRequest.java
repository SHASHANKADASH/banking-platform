package org.shashanka.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Long accountId;
    private Double amount;
    private String merchant;
}
