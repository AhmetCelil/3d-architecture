package com.example.commerce.auth.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterResponseDTO extends BaseResponseDto {

    /** SIRKET kaydında bir kereliğine dönen ham API key; başka hiçbir yerde saklanmaz. */
    private String apiKey;
}
