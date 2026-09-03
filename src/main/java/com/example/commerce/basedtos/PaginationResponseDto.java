package com.example.commerce.basedtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationResponseDto {

    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}
