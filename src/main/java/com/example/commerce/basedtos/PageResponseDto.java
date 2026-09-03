package com.example.commerce.basedtos;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Sayfalı (paginated) liste response'ları için ortak zarf:
 * {@code { "data": [...], "meta": { "pagination": { "pageNo", "pageSize", "totalElements", "totalPages" } } } }
 * Kullanım şekli iki türlü olabilir:
 *  - EXTEND: {@code class XyzListeleResponseDTO extends PageResponseDto<XyzDTO> {}}
 *  - SARMALAMA: {@code PageResponseDto<XyzDTO> resp = PageResponseDto.of(springPage, this::mapper);}
 */
@Getter
@Setter
public class PageResponseDto<T> extends BaseResponseDto {

    private List<T> data;
    private MetaResponseDto meta;

    public void loadFrom(Page<T> sourcePage) {
        loadFrom(sourcePage, Function.identity());
    }

    public <E> void loadFrom(Page<E> sourcePage, Function<E, T> mapper) {
        this.data = sourcePage.getContent().stream().map(mapper).toList();

        PaginationResponseDto pagination = new PaginationResponseDto();
        pagination.setPageNo(sourcePage.getNumber() + 1);
        pagination.setPageSize(sourcePage.getSize());
        pagination.setTotalElements(sourcePage.getTotalElements());
        pagination.setTotalPages(sourcePage.getTotalPages());

        MetaResponseDto metaDto = new MetaResponseDto();
        metaDto.setPagination(pagination);
        this.meta = metaDto;
    }

    public static <T> PageResponseDto<T> of(Page<T> sourcePage) {
        PageResponseDto<T> dto = new PageResponseDto<>();
        dto.loadFrom(sourcePage);
        return dto;
    }

    public static <E, T> PageResponseDto<T> of(Page<E> sourcePage, Function<E, T> mapper) {
        PageResponseDto<T> dto = new PageResponseDto<>();
        dto.loadFrom(sourcePage, mapper);
        return dto;
    }
}
