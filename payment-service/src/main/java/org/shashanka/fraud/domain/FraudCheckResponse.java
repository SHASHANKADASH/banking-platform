package org.shashanka.fraud.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class FraudCheckResponse {
    Boolean approved;
    String reason;
}
