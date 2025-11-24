package com.yowyob.template.infrastructure.adapters.outbound.cache;

import com.yowyob.template.domain.model.Product;
import com.yowyob.template.domain.ports.out.ProductCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
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