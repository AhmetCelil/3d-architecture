package com.example.commerce.basedtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationRequestDto {

    private int pageNo = 1;
    private int pageSize = 20;
}
