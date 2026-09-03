package com.example.commerce.adminpanel.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Ünite tipi ekleme/güncelleme için ortak istek gövdesi. */
@Getter
@Setter
public class UniteTipiInputDTO {
    private String blockLabel;
    private String label;
    private Double area;
    private Integer roomCount;
    private String description;
    private List<FloorPlanRoomDetailDTO> roomDetails;

    /** "floorPlans" / "newFloorPlans" multipart dosya listesiyle index bazında eşleşen opsiyonel başlıklar (örn. "Zemin Kat"). */
    private List<String> floorPlanTitles;
}
