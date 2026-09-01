package com.example.commerce.superadminpanel.dto;

import com.example.commerce.basedtos.PageRequestDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SirketleriListeleRequestDTO extends PageRequestDto {
    /** null/boşsa filtre uygulanmaz, doluysa şirket adında case-insensitive arama yapılır. */
    private String search;
}
