package com.yowyob.fleet.domain.exception;

public class StockFullException extends RuntimeException {
    public StockFullException(String message) {
        super(message);
    }
}