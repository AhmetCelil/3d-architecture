package com.example.commerce.adminpanel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profilePicture;
    private String description;
    private String role;
}