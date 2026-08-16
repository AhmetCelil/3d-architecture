package com.example.commerce.adminpanel.dto;

import com.example.commerce.basedtos.PageRequestDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IletisimMesajlariListeleRequestDTO extends PageRequestDto {
    /** null ise tümü, true/false verilirse okundu/okunmadı filtresi uygulanır. */
    private Boolean read;
}
