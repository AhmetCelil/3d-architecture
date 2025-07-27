package com.example.commerce.auth.service;

import com.example.commerce.auth.dto.UserLogInRequestDTO;
import com.example.commerce.auth.dto.UserLogInResponseDTO;
import com.example.commerce.auth.dto.UserRegisterRequestDTO;
import com.example.commerce.auth.dto.UserRegisterResponseDTO;

public interface AuthService {
    UserRegisterResponseDTO register(UserRegisterRequestDTO requestDTO);
    UserLogInResponseDTO login(UserLogInRequestDTO requestDTO);
}
