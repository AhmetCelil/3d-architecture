package com.example.commerce.adminpanel.controller;

import com.example.commerce.adminpanel.dto.UserInfoResponseDTO;
import com.example.commerce.adminpanel.service.AdminProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProfilAyarlarıController {

    private final AdminProfileService profileService;

    @GetMapping("/user-info")
    public ResponseEntity<UserInfoResponseDTO> getCurrentUserInfo(Principal principal) {
        String email = principal.getName();
        UserInfoResponseDTO userInfo = profileService.getAdminUserInfo(email);
        return ResponseEntity.ok(userInfo);
    }
}