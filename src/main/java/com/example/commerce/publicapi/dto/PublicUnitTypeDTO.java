package com.example.commerce.publicapi.dto;

import com.example.commerce.adminpanel.dto.FloorPlanRoomDetailDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PublicUnitTypeDTO {
    private Long id;
    private String blockLabel;
    private String label;
    private Double area;
    private Integer roomCount;
    private String description;
    private List<FloorPlanRoomDetailDTO> roomDetails;
    private List<PublicDosyaMetaDTO> files;
}
