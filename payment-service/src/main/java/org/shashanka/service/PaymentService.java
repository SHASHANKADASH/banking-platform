package org.shashanka.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.shashanka.domain.PaymentProcessedEvent;
import org.shashanka.exception.FraudDetectedException;
import org.shashanka.exception.InsufficientBalanceException;
import org.shashanka.exception.ResourceNotFoundException;
import org.shashanka.domain.PaymentRequest;
import org.shashanka.domain.PaymentResponse;
import org.shashanka.entity.AccountModel;
import org.shashanka.entity.PaymentModel;
import org.shashanka.fraud.domain.FraudCheckRequest;
import org.shashanka.fraud.domain.FraudCheckResponse;
import org.shashanka.kafka.producer.PaymentEventProducer;
import org.shashanka.repository.AccountRepository;
import org.shashanka.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final RestClient fraudServiceRestClient;
    private final PaymentEventProducer paymentEventProducer;

    // follows proxy pattern in spring. Enables atomicity
    @Transactional
    public PaymentResponse processPayment(final PaymentRequest payment) {
        log.info("Thread: {}", Thread.currentThread().getName());
        final AccountModel account = accountRepository.findById(payment.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        addDelay(2000);
        final double remainingBalance = account.getBalance() - payment.getAmount();
        if(remainingBalance < 0) {
            throw new InsufficientBalanceException("Insufficient Balance");
        }
        log.info("Balance before update {}", account.getBalance());
        final FraudCheckResponse fraudCheckResponse = getFraudCheckResponse(payment);
        if (Objects.isNull(fraudCheckResponse) || !fraudCheckResponse.getApproved()) {
            throw new FraudDetectedException("Fraud detected");
        }
        account.setBalance(remainingBalance);
        accountRepository.save(account);
        log.info("Balance after update {}", account.getBalance());
        final PaymentModel paymentModel = getPayment(payment, account.getId());
        paymentRepository.save(paymentModel);
        paymentEventProducer.publishEvent(getPaymentProcessedEvent(payment, paymentModel));
        return PaymentResponse.builder().paymentId(paymentModel.getId())
                .status(paymentModel.getStatus())
                .remainingBalance(account.getBalance()).build();
    }

    private static PaymentProcessedEvent getPaymentProcessedEvent(PaymentRequest payment, PaymentModel paymentModel) {
        return new PaymentProcessedEvent(
                paymentModel.getId(),
                paymentModel.getAccountId(),
                paymentModel.getAmount(),
                payment.getMerchant(),
                paymentModel.getStatus()
        );
    }

    private static PaymentModel getPayment(PaymentRequest payment, Long id) {
        return PaymentModel.builder()
                .amount(payment.getAmount())
                .accountId(id)
                .merchant(payment.getMerchant())
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static void addDelay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private FraudCheckResponse getFraudCheckResponse(final PaymentRequest payment) {
        return fraudServiceRestClient.post()
                .uri("/fraud/check")
                .body(new FraudCheckRequest(payment.getAccountId(), payment.getAmount(), payment.getMerchant()))
                .retrieve()
                .body(FraudCheckResponse.class);
    }
}
