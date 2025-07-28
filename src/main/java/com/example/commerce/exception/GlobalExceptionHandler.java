package com.example.commerce.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.commerce.basedtos.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseResponseDto> handleAppException(AppException ex) {
        BaseResponseDto response = new BaseResponseDto() {};
        response.setMessages(ex.getMessages());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }
}
