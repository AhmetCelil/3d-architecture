package com.example.commerce.basedtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaRequestDto {

    private PaginationRequestDto pagination = new PaginationRequestDto();
}
