package com.example.commerce.basedtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BaseRequestDto {
    private List<AppMessageDto> messages;

    public BaseRequestDto() {
    }

    public BaseRequestDto(List<AppMessageDto> messages) {
        this.messages = messages;
    }
}
