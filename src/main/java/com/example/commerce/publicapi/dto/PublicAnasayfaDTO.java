package com.example.commerce.publicapi.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicAnasayfaDTO {
    private String homepageTitle;
    private String homepageSubtitle;
    private Integer completedProjectsCount;
    private Integer ongoingProjectsCount;
    private Integer experienceYears;
    private Integer happyClientsCount;
}
