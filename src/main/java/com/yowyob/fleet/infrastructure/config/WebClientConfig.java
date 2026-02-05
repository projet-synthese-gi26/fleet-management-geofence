package com.yowyob.fleet.infrastructure.config;

import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class WebClientConfig {

    // --- Filtre de Logging ---
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("📡 [OUTBOUND] {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> log.info("   👉 {}: {}", name, value))
            );
            return Mono.just(clientRequest);
        });
    }

    // --- 1. WebClient manuel pour le Paiement (Celui qui remplace l'interface) ---
    @Bean("paymentWebClient")
    public WebClient paymentWebClient(WebClient.Builder builder,
                                      @Value("${application.external.payment-service-url}") String url) {
        return builder
                .baseUrl(url)
                .filter(logRequest()) // Active les logs
                .build();
    }

    // --- 2. Clients Déclaratifs (Les autres, on les garde) ---

    @Bean
    public VehicleApiClient vehicleApiClient(WebClient.Builder builder,
                                             @Value("${application.external.vehicle-service-url}") String url) {
        WebClient webClient = builder.baseUrl(url).build();
        return createProxy(webClient, VehicleApiClient.class);
    }

    @Bean
    public AuthApiClient authApiClient(WebClient.Builder builder,
                                       @Value("${application.auth.url}") String url) {
        WebClient webClient = builder.baseUrl(url).build();
        return createProxy(webClient, AuthApiClient.class);
    }

    @Bean
    public GeofenceApiClient geofenceApiClient(WebClient.Builder builder,
                                               @Value("${application.external.geofence-service-url}") String url) {
        WebClient webClient = builder.baseUrl(url).build();
        return createProxy(webClient, GeofenceApiClient.class);
    }

    @Bean
    public GeofenceAuthClient geofenceAuthClient(WebClient.Builder builder,
                                                 @Value("${application.external.geofence-service-url}") String url) {
        WebClient webClient = builder.baseUrl(url).build();
        return createProxy(webClient, GeofenceAuthClient.class);
    }

    // TODO: Activer après implémentation du service de notification
    // @Bean
    // public NotificationApiClient notificationApiClient(WebClient.Builder builder,
    //                                                    @Value("${application.external.notification.url}") String url) {
    //     WebClient webClient = builder.baseUrl(url).build();
    //     return createProxy(webClient, NotificationApiClient.class);
    // }

    // Helper générique
    private <S> S createProxy(WebClient webClient, Class<S> serviceClass) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(serviceClass);
    }
}