package org.shashanka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class FraudServiceRestClient {
    @Bean
    RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://fraud-service:8082")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }
}
