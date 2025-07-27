package com.example.commerce.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLogInRequestDTO {
    private String email;
    private String password;
}
