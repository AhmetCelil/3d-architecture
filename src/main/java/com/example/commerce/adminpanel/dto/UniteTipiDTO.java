package com.example.commerce.adminpanel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniteTipiDTO {
    private Long id;
    private String blockLabel;
    private String label;
    private Double area;
    private Integer roomCount;
    private String description;
    private List<FloorPlanRoomDetailDTO> roomDetails;
    private List<ProjectFileMetaDTO> files;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
