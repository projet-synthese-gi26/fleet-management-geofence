package com.yowyob.fleet.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.AuthApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.VehicleApiClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceAuthClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.NotificationApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.PaymentApiClient; 

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public VehicleApiClient vehicleApiClient(WebClient.Builder builder, 
                                             @Value("${application.external.vehicle-service-url}") String url) {
                                            
        WebClient webClient = builder.baseUrl(url).build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        
        return factory.createClient(VehicleApiClient.class);
    }

    @Bean
    public AuthApiClient authApiClient(WebClient.Builder builder, 
                                       @Value("${application.auth.url}") String url) {
        
        WebClient webClient = builder.baseUrl(url).build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        
        return factory.createClient(AuthApiClient.class);
    }
     @Bean
    public GeofenceApiClient geofenceApiClient(                                              @Value("${application.external.geofence-service-url}") String url) {
        // url doit être http://localhost:8081 (SANS le /api/v1 qui est dans l'interface)
        WebClient webClient = createInsecureWebClient(url).build();
        return createProxy(webClient, GeofenceApiClient.class);
    }
     @Bean
    public NotificationApiClient notificationApiClient(WebClient.Builder builder, 
                                                      @Value("${application.notification.url}") String url) {
        WebClient webClient = builder.baseUrl(url).build();
        return createProxy(webClient, NotificationApiClient.class);
    }

    /**
     * Helper pour éviter la répétition du code de factory
     */
    private <S> S createProxy(WebClient webClient, Class<S> serviceClass) {
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(serviceClass);
    }

    @Bean
    public GeofenceAuthClient geofenceAuthClient(@Value("${application.external.geofence-service-url}") String url) {
        WebClient webClient = createInsecureWebClient(url).build();
        return createProxy(webClient, GeofenceAuthClient.class);
    }

    @Bean
    public PaymentApiClient paymentApiClient(WebClient.Builder builder,
                                             @Value("${application.external.payment-service-url}") String url) {
        return createProxy(builder.baseUrl(url).build(), PaymentApiClient.class);
    }

    // Helper pour créer un WebClient qui ignore les erreurs SSL (DEV UNIQUEMENT)
    private WebClient.Builder createInsecureWebClient(String baseUrl) {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create()
                    .secure(t -> t.sslContext(sslContext));

            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClient));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}