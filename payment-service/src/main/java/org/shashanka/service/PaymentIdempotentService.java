package org.shashanka.service;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.shashanka.domain.PaymentCompletedEvent;
import org.shashanka.domain.PaymentRequest;
import org.shashanka.domain.PaymentResponse;
import org.shashanka.entity.AccountModel;
import org.shashanka.entity.IdempotencyRecordModel;
import org.shashanka.entity.PaymentModel;
import org.shashanka.exception.FraudDetectedException;
import org.shashanka.exception.InsufficientBalanceException;
import org.shashanka.exception.ResourceNotFoundException;
import org.shashanka.fraud.service.FraudService;
import org.shashanka.repository.AccountRepository;
import org.shashanka.repository.IdempotencyRepository;
import org.shashanka.repository.PaymentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Log4j2
public class PaymentIdempotentService {
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final FraudService fraudService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PaymentIdempotentService(PaymentRepository paymentRepository,
                                    AccountRepository accountRepository,
                                    IdempotencyRepository idempotencyRepository,
                                    FraudService fraudService,
                                    ApplicationEventPublisher applicationEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.fraudService = fraudService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    // follows proxy pattern in spring. Enables atomicity
    @Transactional
    public PaymentResponse processPayment(final String idempotencyKey, final PaymentRequest payment) {
        log.info("Thread: {}", Thread.currentThread().getName());
        final Optional<IdempotencyRecordModel> idempotencyRecordModel = idempotencyRepository.findById(idempotencyKey);
        if(idempotencyRecordModel.isPresent()) {
            final PaymentModel paymentModel = paymentRepository.findById(idempotencyRecordModel.get().getPaymentId()).orElseThrow();
            return PaymentResponse.builder().paymentId(paymentModel.getId())
                    .status(paymentModel.getStatus())
                    .build();
        }
        final AccountModel account = accountRepository.findById(payment.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        final Double paymentAmount = payment.getAmount();
        final double remainingBalance = account.getBalance() - paymentAmount;
        if(remainingBalance < 0) {
            throw new InsufficientBalanceException("Insufficient Balance");
        }
        log.info("Balance before update {}", account.getBalance());
        final boolean transactionAllowed = fraudService.runFraudChecks(account.getId(), paymentAmount, payment.getMerchant());
        if(!transactionAllowed) {
            throw new FraudDetectedException("Fraud detected");
        }
        account.setBalance(remainingBalance);
        accountRepository.save(account);
        log.info("Balance after update {}", account.getBalance());
        final PaymentModel paymentModel = getPayment(payment, account.getId());
        paymentRepository.save(paymentModel);
        final IdempotencyRecordModel idempotencyRecordModel1 = IdempotencyRecordModel.builder()
                .idempotencyKey(idempotencyKey).paymentId(paymentModel.getId()).createdAt(LocalDateTime.now()).build();
        idempotencyRepository.save(idempotencyRecordModel1);
        final PaymentCompletedEvent paymentCompletedEvent = PaymentCompletedEvent
                .builder().paymentId(paymentModel.getId()).accountId(account.getId()).amount(paymentAmount).build();
        applicationEventPublisher.publishEvent(paymentCompletedEvent);
        return PaymentResponse.builder().paymentId(paymentModel.getId())
                .status(paymentModel.getStatus())
                .remainingBalance(account.getBalance()).build();
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
}
