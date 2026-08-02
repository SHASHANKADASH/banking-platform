package org.shashanka.fraud.domain;

import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class FraudCheckResponse {
    Boolean approved;
    String reason;
}
