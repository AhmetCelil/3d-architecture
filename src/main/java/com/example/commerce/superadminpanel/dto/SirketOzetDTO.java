package com.example.commerce.superadminpanel.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SirketOzetDTO {
    private Long id;
    private String name;
    private String taxNumber;
    private String sector;
    private String contactEmail;
    private String contactPhone;
    private boolean active;
    private boolean rentModuleEnabled;
    private long memberCount;
}
