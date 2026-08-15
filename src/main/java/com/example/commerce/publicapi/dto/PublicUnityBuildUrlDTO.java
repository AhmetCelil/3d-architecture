package com.example.commerce.publicapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PublicUnityBuildUrlDTO {
    private String part;
    private String url;
    private long validityMinutes;
}
