package org.shashanka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class FraudServiceRestClient {
    @Bean
    RestClient fraudServiceRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8082")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }
}
