package com.example.commerce.adminpanel.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NedenBizDTO {
    private Long id;
    private String title;
    private String description;
    private String iconKey;
    private Integer displayOrder;
}
