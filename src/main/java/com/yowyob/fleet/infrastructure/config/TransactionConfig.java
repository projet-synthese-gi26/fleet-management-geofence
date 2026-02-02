package com.yowyob.fleet.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.R2dbcTransactionManager; // <-- Le bon import pour Spring Boot 3
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
public class TransactionConfig {

    @Bean
    @Primary
    public ReactiveTransactionManager reactiveTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}