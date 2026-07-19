package org.shashanka.controller;

import org.shashanka.domain.PaymentRequest;
import org.shashanka.domain.PaymentResponse;
import org.shashanka.domain.SimulationRequest;
import org.shashanka.service.PaymentIdempotentService;
import org.shashanka.service.PaymentService;
import org.shashanka.service.PaymentSimulationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentIdempotentService paymentIdempotentService;
    private final PaymentSimulationService paymentSimulationService;

    public PaymentController(PaymentService paymentService,
                             PaymentIdempotentService paymentIdempotentService,
                             PaymentSimulationService paymentSimulationService) {
        this.paymentService = paymentService;
        this.paymentIdempotentService = paymentIdempotentService;
        this.paymentSimulationService = paymentSimulationService;
    }

    @PostMapping
    public PaymentResponse processPayment(final @RequestBody PaymentRequest payment) {
        PaymentResponse paymentResponse = paymentService.processPayment(payment);
        System.out.println(paymentResponse);
        return paymentResponse;
    }

    @PostMapping("/idempotent")
    public PaymentResponse processPayment(final @RequestHeader("x-idempotency-key") String idempotencyKey,
                                          final @RequestBody PaymentRequest payment) {
        return paymentIdempotentService.processPayment(idempotencyKey, payment);
    }

    @PostMapping("/simulate")
    public String simulate(final @RequestBody SimulationRequest simulationRequest) {
        paymentSimulationService.simulate(simulationRequest);
        return "Success";
    }
}
