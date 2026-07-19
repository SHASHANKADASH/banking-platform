package org.shashanka.fraud.service;

import lombok.extern.log4j.Log4j2;
import org.shashanka.fraud.domain.UserRiskProfile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
@Log4j2
public class FraudService {
    private final Map<Long, UserRiskProfile> riskCache = new ConcurrentHashMap<>();
    private final Executor executor;

    public FraudService(@Qualifier("fraud-check") Executor executor) {
        this.executor = executor;
    }

    public boolean runFraudChecks(final Long accountId, Double amount, final String merchant) {
        updateRiskCache(accountId, amount);
        final CompletableFuture<Boolean> velocityCompletableFuture = CompletableFuture.supplyAsync(
                () -> velocity(accountId), executor
        );
        final CompletableFuture<Boolean> amountCompletableFuture = CompletableFuture.supplyAsync(
                () -> amountCheck(amount), executor
        );
        final CompletableFuture<Boolean> merchantCompletableFuture = CompletableFuture.supplyAsync(
                () -> merchantCheck(merchant), executor
        );
        // To know why we should not use .get() follow the below links
        // https://www.baeldung.com/java-completablefuture-allof-join
        // https://www.baeldung.com/java-completablefuture-join-vs-get
        // return velocityCompletableFuture.get() && merchantCompletableFuture.get() && amountCompletableFuture.get();
        return CompletableFuture.allOf(velocityCompletableFuture, amountCompletableFuture, merchantCompletableFuture)
                .thenApply(v -> velocityCompletableFuture.join()
                        && merchantCompletableFuture.join() && amountCompletableFuture.join())
                .join();
    }

    private boolean velocity(final Long accountId) {
        log.info("Check executed by {}", Thread.currentThread().getName());
        return riskCache.get(accountId).getTransactionCount() <= 5;
    }

    private void updateRiskCache(Long accountId, Double amount) {
        riskCache.compute(
                accountId, (id, profile) -> {
                    if (Objects.isNull(profile)) {
                        profile = UserRiskProfile.builder().transactionCount(0).totalAmount(0D)
                                .lastTransactionTime(LocalDateTime.now()).build();
                    }
                    profile.setTransactionCount(profile.getTransactionCount() + 1);
                    profile.setTotalAmount(profile.getTotalAmount() + amount);
                    profile.setLastTransactionTime(LocalDateTime.now());
                    return profile;
                }
        );
        log.info(riskCache);
    }

    private boolean amountCheck(final Double amount) {
        log.info("Check executed by {}", Thread.currentThread().getName());
        return amount < 5000;
    }

    private boolean merchantCheck(String merchant) {
        log.info("Check executed by {}", Thread.currentThread().getName());
        return !merchant.equalsIgnoreCase("SCAM");
    }
}
