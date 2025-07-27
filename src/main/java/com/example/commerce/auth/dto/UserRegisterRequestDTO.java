package com.example.commerce.auth.dto;

import com.example.commerce.auth.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequestDTO {

    private String email;
    private String password;
    private Role role;
}
