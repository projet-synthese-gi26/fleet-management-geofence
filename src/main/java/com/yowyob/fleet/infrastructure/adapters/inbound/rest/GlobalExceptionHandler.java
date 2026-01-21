package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ProblemDetail handleWebClientException(WebClientResponseException ex) {
        // On récupère le corps de la réponse d'erreur distante
        String remoteResponseBody = ex.getResponseBodyAsString();
        
        log.error("Erreur API Distante ({}): {}", ex.getStatusCode(), remoteResponseBody);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), remoteResponseBody);
        problem.setTitle("Erreur du service distant (" + ex.getStatusText() + ")");
        problem.setType(URI.create("about:blank"));
        
        // On peut ajouter des propriétés custom pour le debug
        problem.setProperty("remote_status", ex.getStatusCode().value());
        
        return problem;
    }

    // Garde les autres handlers si tu veux des traitements spécifiques, 
    // mais celui ci-dessus est générique et couvrira tout (400, 401, 500...)
}