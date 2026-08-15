package com.example.commerce.basedtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Sayfalı (paginated) listeleme isteklerinin ortak taban alanları (FE'den
 * query param olarak gelir: {@code ?page=0&size=20}). Her listeleme kendi
 * request DTO'sunu bunu extend ederek tanımlar, örn:
 * {@code class ProjeleriListeleRequestDTO extends PageRequestDto {}}
 * — ileride o listelemeye özel filtre alanları eklenmek istenirse buraya
 * eklenir.
 */
@Getter
@Setter
public class PageRequestDto {

    private int page = 0;
    private int size = 20;

    /** page/size'ı güvenli aralığa (page>=0, 1<=size<=maxSize) çekip Pageable üretir. */
    public Pageable toPageable(int maxSize) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), maxSize);
        return PageRequest.of(safePage, safeSize);
    }
}
