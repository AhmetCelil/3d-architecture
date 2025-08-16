package com.example.commerce.users.controller;

import com.example.commerce.users.service.UserService;
import com.example.commerce.users.dto.CompanyProjectResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/projeler")
    public ResponseEntity<List<CompanyProjectResponseDTO>> getSirketProjeleri(
            @RequestParam String companyEmail) {

        List<CompanyProjectResponseDTO> projects = userService.getSirketProjeleri(companyEmail);
        return ResponseEntity.ok(projects);
    }
}
