package com.example.commerce.exception;

import com.example.commerce.basedtos.AppMessageDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
public class ValidationServiceException extends AppException {

    private String validationSource; // Örneğin: "UserService", "FormInput", vb.

    public ValidationServiceException(String messageCode, String defaultMessage, Object... args) {
        super(HttpStatus.BAD_REQUEST, new AppMessageDto(messageCode, defaultMessage, args));
    }

    public ValidationServiceException(AppMessageDto messageDto) {
        super(HttpStatus.BAD_REQUEST, messageDto);
    }
}