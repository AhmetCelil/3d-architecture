package com.example.commerce.exception;

import org.springframework.http.HttpStatus;

public class BusinessServiceException extends AppException {

    public BusinessServiceException(String messageCode, String defaultMessage, Object... args) {
        super(HttpStatus.BAD_REQUEST, new AppMessageDto(messageCode, defaultMessage, args));
    }

    public BusinessServiceException(AppMessageDto messageDto) {
        super(HttpStatus.BAD_REQUEST, messageDto);
    }
}
