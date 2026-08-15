package com.example.commerce.adminpanel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bir kat planı görseline bağlı oda/alan bazlı bilgi (örn. "Oturma Odası" → "25 m²"). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorPlanRoomDetail {

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "detail_value")
    private String value;
}
