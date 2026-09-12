package com.example.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AdzunaApiException extends RuntimeException {

    private final String errorCode = "ADZ_001";

    public AdzunaApiException(String message) {
        super(message);
    }

    public AdzunaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}