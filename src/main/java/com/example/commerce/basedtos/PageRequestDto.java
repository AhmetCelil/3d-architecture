package com.example.commerce.basedtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Sayfalı (paginated) listeleme isteklerinin ortak zarfı (FE'den JSON body
 * olarak gelir):
 * {@code { "data": {...filtreler}, "meta": { "pagination": { "pageNo": 1, "pageSize": 20 } } } }
 * Her listeleme kendi filtre tipini D olarak vererek bunu extend eder, örn:
 * {@code class ProjeleriListeleRequestDTO extends PageRequestDto<Void> {}}
 * — filtre alanı yoksa D olarak {@code Void} kullanılır.
 */
@Getter
@Setter
public class PageRequestDto<D> extends BaseRequestDto {

    private D data;
    private MetaRequestDto meta = new MetaRequestDto();

    /** pageNo (1 tabanlı) / pageSize'ı güvenli aralığa (page>=0, 1<=size<=maxSize) çekip Pageable üretir. */
    public Pageable toPageable(int maxSize) {
        PaginationRequestDto pagination = meta != null ? meta.getPagination() : null;
        int pageNo = pagination != null ? pagination.getPageNo() : 1;
        int pageSize = pagination != null ? pagination.getPageSize() : maxSize;

        int safePage = Math.max(pageNo - 1, 0);
        int safeSize = Math.min(Math.max(pageSize, 1), maxSize);
        return PageRequest.of(safePage, safeSize);
    }
}
