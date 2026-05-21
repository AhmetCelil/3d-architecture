package com.example.commerce.altcha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AltchaChallengeDTO {
    private String algorithm;
    private String challenge;
    private String salt;
    private String signature;

}
