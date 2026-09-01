package com.example.commerce.superadminpanel.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SirketDetayDTO {
    private Long id;
    private String name;
    private String taxNumber;
    private String sector;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private boolean active;
    private boolean rentModuleEnabled;
    private List<UyeOzetDTO> members;
}
