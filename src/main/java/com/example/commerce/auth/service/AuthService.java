package com.example.commerce.auth.service;

import com.example.commerce.auth.dto.*;

public interface AuthService {
    UserRegisterResponseDTO register(UserRegisterRequestDTO requestDTO);
    UserLogInResponseDTO login(UserLogInRequestDTO requestDTO);
    UserLogOutResponseDTO logOut(UserLogOutRequestDTO requestDTO);
}
