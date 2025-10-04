package com.example.commerce.awss3.controller;

import com.example.commerce.awss3.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    /**
     * Normal presigned URL (sadece 5 saniye sınırlı)
     */
    @GetMapping("/file-url")
    public ResponseEntity<?> getUnityFileUrl(
            @RequestParam String fileName,
            @RequestParam String userEmail) {

        try {
            String url = s3Service.getUnityFileUrl(fileName, userEmail);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("url", url);
            result.put("validityMinutes", 3);
            result.put("message", "URL başarıyla oluşturuldu. 3 dakika geçerli.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "URL oluşturulamadı: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}