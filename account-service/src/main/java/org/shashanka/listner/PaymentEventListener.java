package org.shashanka.listner;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.shashanka.domain.PaymentCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Log4j2
@ToString
public class PaymentEventListener {

    // Gets executed even if there was any failure
    // Without async this will be executed by tomcat nio thread which asynchronous
    @Async
    @EventListener
    public void handlePaymentCompleted(final PaymentCompletedEvent paymentCompletedEvent) {
        log.info("Payment completed successfully by thread {} for : {}", Thread.currentThread().getName(),
                paymentCompletedEvent.toString());
    }

    // Get executed only if transaction was successful
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompletedWithTransactionalEnabled(final PaymentCompletedEvent paymentCompletedEvent) {
        log.info("Payment completed successfully by thread {} for : {}", Thread.currentThread().getName(),
                paymentCompletedEvent.toString());
    }
}
