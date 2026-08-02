package org.shashanka.fraud.controller;

import lombok.RequiredArgsConstructor;
import org.shashanka.fraud.domain.FraudCheckRequest;
import org.shashanka.fraud.domain.FraudCheckResponse;
import org.shashanka.fraud.service.FraudService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudService fraudService;

    @PostMapping("/check")
    public FraudCheckResponse check(final @RequestBody FraudCheckRequest fraudCheckRequest) {
        final boolean fraudCheckFlag = fraudService.runFraudChecks(
                fraudCheckRequest.getAccountId(), fraudCheckRequest.getAmount(), fraudCheckRequest.getMerchant()
        );
        return new FraudCheckResponse(
                fraudCheckFlag,
                null //TODO: will update later
        );
    }
}
