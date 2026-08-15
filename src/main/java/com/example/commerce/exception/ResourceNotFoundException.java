package com.example.commerce.exception;

import com.example.commerce.basedtos.AppMessageDto;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String messageCode, String defaultMessage, Object... args) {
        super(HttpStatus.NOT_FOUND, new AppMessageDto(messageCode, defaultMessage, args));
    }

    public ResourceNotFoundException(AppMessageDto messageDto) {
        super(HttpStatus.NOT_FOUND, messageDto);
    }
}
