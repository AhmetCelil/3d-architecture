package com.example.commerce.profilayarlama.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SirketProfilAyarlaRequestDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profilePicture;
    private String description;
}
