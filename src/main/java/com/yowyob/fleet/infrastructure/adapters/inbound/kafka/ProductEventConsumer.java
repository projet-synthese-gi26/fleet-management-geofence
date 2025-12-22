package com.yowyob.fleet.infrastructure.adapters.inbound.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.yowyob.fleet.domain.model.Product;

@Slf4j
@Component
public class ProductEventConsumer {

    @Value("${application.kafka.topics.product-events}")
    private String productEventsTopic;

    @KafkaListener(topics = "${application.kafka.topics.product-events}", groupId = "template-group")
    public void consume(Product product) {
        log.info("CONSUMER: I received an event for product with id : {} and price : {}", 
                 product.name(), product.price());
    }
}