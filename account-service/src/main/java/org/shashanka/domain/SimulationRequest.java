package org.shashanka.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {
    private Long accountId;
    private Double amount;
    private Integer numberOfRequests;
}
