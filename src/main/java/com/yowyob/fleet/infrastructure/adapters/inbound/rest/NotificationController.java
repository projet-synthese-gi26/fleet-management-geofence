package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Notification;
import com.yowyob.fleet.domain.ports.out.NotificationHistoryRepositoryPort;
import com.yowyob.fleet.infrastructure.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_ACCOUNT, description = "In-app notifications history")
public class NotificationController {

    private final NotificationHistoryRepositoryPort historyPort;

    @GetMapping
    @Operation(summary = "Get my notifications", description = "Paginated list of notifications for the connected user.")
    public Mono<NotificationHistoryRepositoryPort.PagedResult<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    try {
                        UUID userId = UUID.fromString(auth.getName());
                        return historyPort.getUserNotifications(userId, page, size);
                    } catch (Exception e) {
                        return Mono.error(new IllegalStateException("Invalid User Context"));
                    }
                });
    }
}