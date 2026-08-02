package org.shashanka.fraud.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRiskProfile {
    private Integer transactionCount;
    private Double totalAmount;
    private LocalDateTime lastTransactionTime;
}
