package com.example.commerce.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
public class ValidationServiceException extends AppException {

    private String validationSource; // Örneğin: "UserService", "FormInput", vb.

    public ValidationServiceException(HttpStatus httpStatus, AppMessageDto message) {
        super(httpStatus, message);
    }

    public ValidationServiceException(HttpStatus httpStatus, AppMessageDto message, Throwable cause) {
        super(httpStatus, message, cause);
    }

    public ValidationServiceException(HttpStatus httpStatus, List<AppMessageDto> messages) {
        super(httpStatus, messages);
    }

    public ValidationServiceException(HttpStatus httpStatus, List<AppMessageDto> messages, Throwable cause) {
        super(httpStatus, messages, cause);
    }

    public ValidationServiceException(HttpStatus httpStatus, List<AppMessageDto> messages, String validationSource) {
        super(httpStatus, messages);
        this.validationSource = validationSource;
    }

    public ValidationServiceException(HttpStatus httpStatus, List<AppMessageDto> messages, String validationSource, Throwable cause) {
        super(httpStatus, messages, cause);
        this.validationSource = validationSource;
    }
}