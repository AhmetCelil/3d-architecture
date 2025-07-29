package com.example.commerce.auth.controller;

import com.example.commerce.auth.dto.UserLogInRequestDTO;
import com.example.commerce.auth.dto.UserLogInResponseDTO;
import com.example.commerce.auth.dto.UserRegisterRequestDTO;
import com.example.commerce.auth.dto.UserRegisterResponseDTO;
import com.example.commerce.auth.service.AuthService;
import com.example.commerce.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // anotasyonla yönlendirme yap @PreAuthorize("hasAuthority('SATICI')")

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDTO> register(@RequestBody UserRegisterRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.register(requestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLogInResponseDTO> login(@RequestBody UserLogInRequestDTO requestDTO) {
        return ResponseEntity.ok(authService.login(requestDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        try {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Refresh token süresi dolmuş.");
            }

            String roleWithPrefix = userDetails.getAuthorities()
                    .stream()
                    .findFirst()
                    .get()
                    .getAuthority();

            String role = roleWithPrefix.startsWith("ROLE_") ? roleWithPrefix.substring(5) : roleWithPrefix;

            String newAccessToken = jwtService.generateAccessToken(username, role);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token geçersiz.");
        }
    }
}
