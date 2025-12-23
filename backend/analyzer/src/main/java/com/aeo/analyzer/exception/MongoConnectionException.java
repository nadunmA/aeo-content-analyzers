package com.aeo.analyzer.exception;

/**
 * Custom exception for MongoDB connection failures
 */
public class MongoConnectionException extends RuntimeException {

    public MongoConnectionException(String message) {
        super(message);
    }

    public MongoConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}