package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yowyob.fleet.domain.exception.StockFullException;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockFullException.class)
    public ProblemDetail handleStockException(StockFullException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Stock Overflow");
        problem.setType(URI.create("errors/stock-full"));
        return problem;
    }
}