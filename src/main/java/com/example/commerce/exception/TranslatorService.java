package com.example.commerce.exception;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class TranslatorService {

    private final MessageSource messageSource;

    public TranslatorService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String toLocale(String code, Object[] args) {
        return code;
    }
}
