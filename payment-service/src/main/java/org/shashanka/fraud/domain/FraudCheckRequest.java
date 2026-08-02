package org.shashanka.fraud.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FraudCheckRequest {
    Long accountId;
    double amount;
    String merchant;
}
