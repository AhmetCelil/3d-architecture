package com.example.commerce.publicapi.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicHakkimizdaDTO {
    private String aboutTitle;
    private String aboutDescription;
    private String mission;
    private String vision;
    private Integer foundedYear;
    private String story;
    private List<PublicDegerDTO> values;
    private List<PublicNedenBizDTO> whyUs;
    private List<PublicEkipUyesiDTO> team;
}
