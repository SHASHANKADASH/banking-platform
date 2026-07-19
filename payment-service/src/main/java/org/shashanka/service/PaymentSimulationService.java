package org.shashanka.service;

import lombok.extern.log4j.Log4j2;
import org.shashanka.domain.PaymentRequest;
import org.shashanka.domain.SimulationRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Log4j2
public class PaymentSimulationService {
    private final Executor executorService;
    private final PaymentService paymentService;

    public PaymentSimulationService(@Qualifier("default") Executor executorService, PaymentService paymentService) {
        this.executorService = executorService;
        this.paymentService = paymentService;
    }

    public void simulate(final SimulationRequest simulationRequest) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for(int i=0; i< simulationRequest.getNumberOfRequests(); i++) {
            CompletableFuture<Void> completableFuture =
                    CompletableFuture.runAsync(() -> {
                        try {
                            PaymentRequest paymentRequest = PaymentRequest.builder()
                                    .merchant("Flipkart").accountId(simulationRequest.getAccountId())
                                    .amount(simulationRequest.getAmount())
                                    .build();
                            paymentService.processPayment(paymentRequest);
                        } catch (Exception e) {
                            log.error("{} Failed with: {}", Thread.currentThread().getName(), e);
                        }
                    }, executorService);
            futures.add(completableFuture);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();
    }
}
