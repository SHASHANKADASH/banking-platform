package org.shashanka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PaymentFraudDetectionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentFraudDetectionServiceApplication.class, args);
	}

}
