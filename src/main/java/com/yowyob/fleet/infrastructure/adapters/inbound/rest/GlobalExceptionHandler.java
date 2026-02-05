package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions pour l'API.
 * Utilise le standard RFC 7807 (Problem Details for HTTP APIs).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs provenant des appels aux services distants (Auth, Vehicle Service, etc.)
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ProblemDetail handleWebClientException(WebClientResponseException ex) {
        String remoteResponseBody = ex.getResponseBodyAsString();
        log.error("❌ Erreur API Distante ({}): {}", ex.getStatusCode(), remoteResponseBody);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), remoteResponseBody);
        problem.setTitle("Erreur Service Distant");
        problem.setType(URI.create("about:blank"));
        
        // Ajout de l'URL pour faciliter le debug
        if (ex.getRequest() != null) {
            problem.setProperty("remote_url", ex.getRequest().getURI().toString());
        }
        
        return problem;
    }

    /**
     * Gère les erreurs de validation des DTOs (ex: @NotBlank, @NotNull)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidationException(WebExchangeBindException ex) {
        String errors = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("⚠️ Erreur de Validation : {}", errors);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        problem.setTitle("Validation Failed");
        problem.setProperty("fields", ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (a, b) -> a)));
        
        return problem;
    }

    /**
     * Gère les exceptions métier lancées via ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        log.warn("💡 Erreur Métier : {}", ex.getReason());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        
        // Fix Spring Boot 3 : HttpStatusCode n'a pas getReasonPhrase(), on cast en HttpStatus
        HttpStatusCode status = ex.getStatusCode();
        if (status instanceof HttpStatus httpStatus) {
            problem.setTitle(httpStatus.getReasonPhrase());
        } else {
            problem.setTitle("Business Logic Error");
        }
        
        return problem;
    }

    /**
     * Gère toutes les autres exceptions non capturées (Erreurs 500)
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        log.error("💥 Erreur Serveur Non Gérée", ex);
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Une erreur technique est survenue. Veuillez contacter l'administrateur."
        );
        problem.setTitle("Internal Server Error");
        
        // On peut ajouter la classe de l'exception pour aider au debug en dev
        problem.setProperty("exception_class", ex.getClass().getSimpleName());
        if (ex.getMessage() != null) {
            problem.setProperty("exception_message", ex.getMessage());
        }
        
        return problem;
    }
}