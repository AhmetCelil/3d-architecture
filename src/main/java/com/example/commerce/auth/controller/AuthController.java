package com.example.commerce.auth.controller;

import com.example.commerce.auth.dto.UserLogInRequestDTO;
import com.example.commerce.auth.dto.UserLogInResponseDTO;
import com.example.commerce.auth.dto.UserRegisterRequestDTO;
import com.example.commerce.auth.dto.UserRegisterResponseDTO;
import com.example.commerce.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // anotasyonla yönlendirme yap @PreAuthorize("hasAuthority('SATICI')")

    @PreAuthorize("hasAuthority('SATICI')")
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDTO> register(@RequestBody UserRegisterRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.register(requestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLogInResponseDTO> login(@RequestBody UserLogInRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.login(requestDTO));
    }
}
