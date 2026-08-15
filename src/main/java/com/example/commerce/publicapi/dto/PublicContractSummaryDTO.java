package com.example.commerce.publicapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Finansal veri içerir - yalnızca API key ile firma erişiminde döner, unique-code ile asla. */
@Getter
@Setter
@Builder
public class PublicContractSummaryDTO {
    private String contractNumber;
    private String contractType;
    private String status;
    private BigDecimal contractAmount;
    private String currency;
}
