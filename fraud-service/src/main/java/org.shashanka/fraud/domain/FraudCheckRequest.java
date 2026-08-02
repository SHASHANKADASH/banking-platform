package org.shashanka.fraud.domain;

import lombok.Getter;

@Getter
public class FraudCheckRequest {
    Long accountId;
    double amount;
    String merchant;
}
