package com.example.commerce.adminpanel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * "floorPlans" / "newFloorPlans" multipart dosya listesiyle index bazında
 * eşleşen, her kat planı görseline ait başlık + oda detayları girişi.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FloorPlanInputDTO {
    private String title;
    private List<FloorPlanRoomDetailDTO> roomDetails;
}
