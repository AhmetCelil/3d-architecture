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
     * Unity dosyaları için presigned URL
     */
    @GetMapping("/file-url")
    public ResponseEntity<?> getUnityFileUrl(
            @RequestParam String fileName,
            @RequestParam String userEmail) {

        try {
            System.out.println("📥 URL isteği: " + fileName + " - " + userEmail);

            String url = s3Service.getUnityFileUrl(fileName, userEmail);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("url", url);
            result.put("fileName", fileName);
            result.put("validityMinutes", 15);
            result.put("message", "URL başarıyla oluşturuldu. 15 dakika geçerli.");

            System.out.println("✅ URL başarıyla döndürüldü");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("❌ Hata: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "URL oluşturulamadı: " + e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            error.put("fileName", fileName);

            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Debug için - Kullanıcının tüm dosyalarını listele
     */
    @GetMapping("/debug-files")
    public ResponseEntity<?> debugFiles(@RequestParam String userEmail) {
        try {
            System.out.println("🔍 " + userEmail + " dosyaları listeleniyor...");
            s3Service.listBucketContents(userEmail + "/");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Dosya listesi backend console'da görüntüleniyor");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}