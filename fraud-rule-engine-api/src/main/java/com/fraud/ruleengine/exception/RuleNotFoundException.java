package com.fraud.ruleengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RuleNotFoundException extends RuntimeException {

    public RuleNotFoundException(Long id) {
        super("Rule not found with id: " + id);
    }

    public RuleNotFoundException(String message) {
        super(message);
    }
}
