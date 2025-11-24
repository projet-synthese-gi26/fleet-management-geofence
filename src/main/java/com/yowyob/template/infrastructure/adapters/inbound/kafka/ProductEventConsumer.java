package com.yowyob.template.infrastructure.adapters.inbound.kafka;

import com.yowyob.template.domain.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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