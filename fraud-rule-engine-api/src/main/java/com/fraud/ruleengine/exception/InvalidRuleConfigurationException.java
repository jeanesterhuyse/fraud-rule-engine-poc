package com.fraud.ruleengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRuleConfigurationException extends RuntimeException {

    public InvalidRuleConfigurationException(String message) {
        super(message);
    }

    public InvalidRuleConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
