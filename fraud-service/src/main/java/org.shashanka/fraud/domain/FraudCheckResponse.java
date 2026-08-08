package org.shashanka.fraud.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@AllArgsConstructor
@Getter
public class FraudCheckResponse {
    Boolean approved;
    String reason;
}
