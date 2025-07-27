package com.example.commerce.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppMessageDto {
    private String code;
    private String text;
    private Object[] args;
}