package com.example.tradelens.exception;



public class InvalidCsvException extends RuntimeException {

    public InvalidCsvException(String message, Throwable cause) {
        super(message, cause);
    }

}
