package com.yowyob.fleet.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.AuthApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.VehicleApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceAuthClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.NotificationApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.PaymentApiClient; 

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
    public GeofenceApiClient geofenceApiClient(WebClient.Builder builder, 
                                              @Value("${application.external.geofence-service-url}") String url) {
        // url doit être http://localhost:8081 (SANS le /api/v1 qui est dans l'interface)
        WebClient webClient = builder.baseUrl(url).build();
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
    public GeofenceAuthClient geofenceAuthClient(WebClient.Builder builder, 
                                                @Value("${application.external.geofence-service-url}") String url) {
        WebClient webClient = builder.baseUrl(url).build();
        return createProxy(webClient, GeofenceAuthClient.class);
    }

    @Bean
    public PaymentApiClient paymentApiClient(WebClient.Builder builder,
                                             @Value("${application.external.payment-service-url}") String url) {
        return createProxy(builder.baseUrl(url).build(), PaymentApiClient.class);
    }

}