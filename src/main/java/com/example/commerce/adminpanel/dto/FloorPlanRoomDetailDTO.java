package com.example.commerce.adminpanel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bir kat planı görseline bağlı oda/alan bazlı bilgi (örn. "Oturma Odası" → "25 m²"). */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorPlanRoomDetailDTO {
    private String roomName;
    private String value;
}
