package com.yowyob.fleet.infrastructure.config;

import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
public class WebClientConfig {

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("📡 [OUTBOUND] {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    // Bean utilisé par PaymentApiAdapter via @Qualifier("paymentWebClient")
    @Bean("paymentWebClient")
    public WebClient paymentWebClient(WebClient.Builder builder,
                                      @Value("${application.external.payment-service-url}") String url) {
        return builder.baseUrl(url).filter(logRequest()).build();
    }

    @Bean
    public VehicleApiClient vehicleApiClient(WebClient.Builder builder,
                                             @Value("${application.external.vehicle-service-url}") String url) {
        return createProxy(builder.baseUrl(url).build(), VehicleApiClient.class);
    }

    @Bean
    public AuthApiClient authApiClient(WebClient.Builder builder,
                                       @Value("${application.auth.url}") String url) {
        return createProxy(builder.baseUrl(url).build(), AuthApiClient.class);
    }

    @Bean
    public GeofenceApiClient geofenceApiClient(@Value("${application.external.geofence-service-url}") String url) {
        WebClient webClient = createInsecureWebClient(url).build();
        return createProxy(webClient, GeofenceApiClient.class);
    }

    @Bean
    public NotificationApiClient notificationApiClient(WebClient.Builder builder, 
                                                      @Value("${application.notification.url}") String url) {
        return createProxy(builder.baseUrl(url).build(), NotificationApiClient.class);
    }

    @Bean
    public GeofenceAuthClient geofenceAuthClient(@Value("${application.external.geofence-service-url}") String url) {
        WebClient webClient = createInsecureWebClient(url).build();
        return createProxy(webClient, GeofenceAuthClient.class);
    }

    private <S> S createProxy(WebClient webClient, Class<S> serviceClass) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(serviceClass);
    }

    private WebClient.Builder createInsecureWebClient(String baseUrl) {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
            return WebClient.builder().baseUrl(baseUrl).clientConnector(new ReactorClientHttpConnector(httpClient));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}