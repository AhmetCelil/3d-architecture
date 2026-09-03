package com.example.commerce.adminpanel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IletisimMesajlariListeleFiltreDTO {

    /** null ise tümü, true/false verilirse okundu/okunmadı filtresi uygulanır. */
    private Boolean read;
}
