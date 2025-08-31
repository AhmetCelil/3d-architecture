package com.example.commerce.users.controller;

import com.example.commerce.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/unity-files")
    public ResponseEntity<String> getUnityFileUrl(@RequestParam String fileName,
                                                  @RequestParam String userEmail) {
        try {
            String fileUrl = userService.getUnityFileUrl(fileName, userEmail);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Dosya bulunamadı: " + e.getMessage());
        }
    }

    // Test endpoint'i
    @GetMapping("/test-minio")
    public ResponseEntity<String> testMinio() {
        try {
            String result = userService.testMinIOConnection();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("MinIO Test Hatası: " + e.getMessage());
        }
    }
}