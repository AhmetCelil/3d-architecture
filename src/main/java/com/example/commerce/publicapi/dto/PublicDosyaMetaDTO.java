package com.example.commerce.publicapi.dto;

import com.example.commerce.adminpanel.dto.FloorPlanRoomDetailDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PublicDosyaMetaDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String title;
    private String fileCategory;
    private List<FloorPlanRoomDetailDTO> roomDetails;
}
