package com.example.commerce.adminpanel.controller;

import com.example.commerce.adminpanel.dto.AdminSirketProjeEkleRequestDTO;
import com.example.commerce.adminpanel.dto.AdminSirketProjeEkleResponseDTO;
import com.example.commerce.adminpanel.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin-profil")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/sirket-proje-ekle")
    public ResponseEntity<AdminSirketProjeEkleResponseDTO> adminSirketProjeEkle(
            @RequestPart("data") AdminSirketProjeEkleRequestDTO requestDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        AdminSirketProjeEkleResponseDTO response = adminService.adminSirketProjeEkle(requestDTO, file);
        return ResponseEntity.ok(response);
    }



}
