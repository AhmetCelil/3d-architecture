package com.example.commerce.adminpanel.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class IletisimBilgileriGuncelleRequestDTO {
    private String address;
    private String phone;
    private String phoneSecondary;
    private String email;
    private String whatsappNumber;
    private String whatsappApiKey;
    private String instagramUrl;
    private String twitterUrl;
    private String facebookUrl;
    private String linkedinUrl;
    private String youtubeUrl;
    private BigDecimal mapLatitude;
    private BigDecimal mapLongitude;
    private String mapEmbedUrl;
    private List<CalismaSaatiDTO> workingHours;
}
