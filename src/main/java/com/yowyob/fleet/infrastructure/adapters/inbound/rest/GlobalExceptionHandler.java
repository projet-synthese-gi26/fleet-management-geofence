package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.URI;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * LE COEUR : Gère n'importe quelle exception du domaine (Auth, Fleet, Trip, etc.)
     */
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        log.warn("⚠️ Business Exception [{}] : {}", ex.getBusinessCode(), ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle("Business Error");
        problem.setProperty("code", ex.getBusinessCode());
        problem.setProperty("timestamp", ex.getTimestamp());
        return problem;
    }

    /**
     * Gère les erreurs de validation des DTOs (@Valid)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidationException(WebExchangeBindException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details);
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://traensys.com/errors/validation"));
        return problem;
    }

    /**
     * FALLBACK : Toutes les autres erreurs imprévues
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        log.error("💥 Critical error caught : ", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, 
            "Une erreur technique imprévue est survenue sur le serveur.");
        problem.setTitle("Internal Server Error");
        return problem;
    }

    /**
     * Gère les exceptions de Spring
     */
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(org.springframework.web.server.ResponseStatusException ex) {
        // On respecte le statut demandé par Spring mais on reste propre
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        problem.setTitle("Protocol Error");
        return problem;
    }
}