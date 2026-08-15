package com.example.commerce.basedtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Sayfalı (paginated) liste response'ları için ortak taban.
 * Kullanım şekli iki türlü olabilir:
 *  - EXTEND: {@code class XyzListeleResponseDTO extends PageResponseDto<XyzDTO> {}}
 *  - SARMALAMA: {@code PageResponseDto<XyzDTO> resp = PageResponseDto.of(springPage, this::mapper);}
 */
@Getter
@Setter
public class PageResponseDto<T> extends BaseResponseDto {

    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public void loadFrom(Page<T> sourcePage) {
        loadFrom(sourcePage, Function.identity());
    }

    public <E> void loadFrom(Page<E> sourcePage, Function<E, T> mapper) {
        this.data = sourcePage.getContent().stream().map(mapper).toList();
        this.page = sourcePage.getNumber();
        this.size = sourcePage.getSize();
        this.totalElements = sourcePage.getTotalElements();
        this.totalPages = sourcePage.getTotalPages();
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
