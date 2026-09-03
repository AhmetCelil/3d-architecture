package com.example.commerce.superadminpanel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SirketleriListeleFiltreDTO {

    /** null/boşsa filtre uygulanmaz, doluysa şirket adında case-insensitive arama yapılır. */
    private String search;
}
