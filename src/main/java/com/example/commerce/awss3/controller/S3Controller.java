package com.example.commerce.awss3.controller;


import com.example.commerce.awss3.dto.LimitedUrlResponse;
import com.example.commerce.awss3.service.S3Service;
import com.example.commerce.awss3.dto.UrlStatusResponse;
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
     * Sınırlı kullanım URL'si oluşturur (3 dakika, maksimum 2 kullanım)
     */
    @GetMapping("/file-url-limited")
    public ResponseEntity<?> getUnityFileLimitedUrl(
            @RequestParam String fileName,
            @RequestParam String userEmail) {

        try {
            LimitedUrlResponse response = s3Service.getUnityFileUrlWithLimits(fileName, userEmail);

            // JSON formatında döndür
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("url", response.getUrl());
            result.put("urlId", response.getUrlId());
            result.put("maxUsage", response.getMaxUsage());
            result.put("validityMinutes", response.getValidityMinutes());
            result.put("message", "URL başarıyla oluşturuldu. 3 dakika geçerli ve maksimum 2 kez kullanılabilir.");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "URL oluşturulamadı: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * URL kullanım durumunu kontrol eder
     */
    @GetMapping("/url-status")
    public ResponseEntity<?> getUrlStatus(@RequestParam String urlId) {
        try {
            UrlStatusResponse status = s3Service.getUrlStatus(urlId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("canUse", status.isCanUse());
            result.put("remainingUsage", status.getRemainingUsage());
            result.put("expired", status.isExpired());
            result.put("message", status.getMessage());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "URL durumu kontrol edilemedi: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * URL kullanımını doğrular ve kullanım sayısını artırır
     */
    @PostMapping("/use-url")
    public ResponseEntity<?> useUrl(@RequestParam String urlId) {
        try {
            boolean canUse = s3Service.validateAndUseUrl(urlId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", canUse);

            if (canUse) {
                result.put("message", "URL başarıyla kullanıldı.");
                // Güncel durumu da döndür
                UrlStatusResponse status = s3Service.getUrlStatus(urlId);
                result.put("remainingUsage", status.getRemainingUsage());
            } else {
                result.put("message", "URL kullanılamaz. Süre dolmuş olabilir veya kullanım limiti aşılmış olabilir.");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "URL kullanımı doğrulanamadı: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Normal presigned URL (sadece 3 dakika sınırlı)
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

    /**
     * Kullanıcının dosyalarını listeler
     */
    @GetMapping("/files")
    public ResponseEntity<?> listUserFiles(@RequestParam String userEmail) {
        try {
            var files = s3Service.listUserUnityFiles(userEmail);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("files", files);
            result.put("count", files.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Dosyalar listelenemedi: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}