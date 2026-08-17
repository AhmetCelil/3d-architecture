package com.example.commerce.rent.admin.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

/** Platform admin'in bir şirket için rent modülünü açıp kapatması amacıyla kullanılır. */
@Getter
@Setter
public class RentModulDurumDTO extends BaseResponseDto {
    private Long companyId;
    private boolean enabled;
}
