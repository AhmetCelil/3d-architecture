package com.example.commerce.util;


import com.example.commerce.basedtos.AppMessageDto;
import com.example.commerce.basedtos.AppMessageType;

public class AppMessageUtil {

    public static AppMessageDto createWithText(String text, AppMessageType type) {
        return AppMessageDto.builder()
                .text(text)
                .type(type)
                .build();
    }

    public static AppMessageDto createWithCode(String code, AppMessageType type, String... args) {
        return AppMessageDto.builder()
                .code(code)
                .type(type)
                .args(args)
                .build();
    }

    public static AppMessageDto create(String code, String text, AppMessageType type, String... args) {
        return AppMessageDto.builder()
                .code(code)
                .text(text)
                .type(type)
                .args(args)
                .build();
    }
}
