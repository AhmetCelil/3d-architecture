package com.example.commerce.publicapi.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicIletisimDTO {
    private String address;
    private String phone;
    private String phoneSecondary;
    private String email;
    private String whatsappNumber;
    private String instagramUrl;
    private String twitterUrl;
    private String facebookUrl;
    private String linkedinUrl;
    private String youtubeUrl;
    private BigDecimal mapLatitude;
    private BigDecimal mapLongitude;
    private String mapEmbedUrl;
    private List<PublicCalismaSaatiDTO> workingHours;
}
