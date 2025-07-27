package com.example.commerce.basedtos;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public abstract class BaseResponseDto implements Serializable {

    private List<AppMessageDto> messages;

    public BaseResponseDto() {
    }

    public BaseResponseDto(List<AppMessageDto> messages) {
        this.messages = messages;
    }
}
