package com.example.commerce.adminpanel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HakkimizdaGuncelleRequestDTO {
    private String aboutTitle;
    private String aboutDescription;
    private String mission;
    private String vision;
    private Integer foundedYear;
    private String story;
    private String homepageTitle;
    private String homepageSubtitle;
    private Integer completedProjectsCount;
    private Integer ongoingProjectsCount;
    private Integer experienceYears;
    private Integer happyClientsCount;
}
