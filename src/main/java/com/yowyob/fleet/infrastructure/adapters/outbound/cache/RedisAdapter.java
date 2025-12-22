package com.yowyob.fleet.infrastructure.adapters.outbound.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import com.yowyob.fleet.domain.model.Product;
import com.yowyob.fleet.domain.ports.out.ProductCachePort;

import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisAdapter implements ProductCachePort {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<Boolean> saveInCache(Product product) {
        return redisTemplate.opsForValue()
                .set("product:" + product.id(), product, Duration.ofMinutes(10));
    }
}