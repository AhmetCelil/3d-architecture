package com.example.commerce.exception;


import com.example.commerce.basedtos.AppMessageDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class AppException extends RuntimeException {

    private List<AppMessageDto> messages;
    private HttpStatus httpStatus;

    public AppException(HttpStatus httpStatus, AppMessageDto message) {
        this(httpStatus, List.of(message), null);
    }

    public AppException(HttpStatus httpStatus, AppMessageDto message, Throwable cause) {
        this(httpStatus, List.of(message), cause);
    }

    public AppException(HttpStatus httpStatus, List<AppMessageDto> messages) {
        this(httpStatus, messages, null);
    }

    public AppException(HttpStatus httpStatus, List<AppMessageDto> messages, Throwable cause) {
        super(cause);
        this.messages = messages;
        this.httpStatus = httpStatus;
    }
}
