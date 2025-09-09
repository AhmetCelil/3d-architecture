package com.example.commerce.awss3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowCredentials = "false") // Specify exact origin

public class S3Controller {

    private final S3Service s3Service;

    /**
     * Unity dosyasının presigned URL'sini döndürür
     */
    @GetMapping("/unity-files")
    public ResponseEntity<String> getUnityFileUrl(@RequestParam String fileName,
                                                  @RequestParam String userEmail) {
        try {
            String fileUrl = s3Service.getUnityFileUrl(fileName, userEmail);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Dosya bulunamadı: " + e.getMessage());
        }
    }

    /**
     * Unity dosyasının direkt S3 URL'sini döndürür (public bucket için)
     */
    @GetMapping("/unity-files/direct")
    public ResponseEntity<String> getDirectUnityFileUrl(@RequestParam String fileName,
                                                        @RequestParam String userEmail) {
        try {
            String fileUrl = s3Service.getDirectS3Url(fileName, userEmail);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Dosya bulunamadı: " + e.getMessage());
        }
    }

    /**
     * Kullanıcının Unity dosyalarını listeler
     */
    @GetMapping("/unity-files/list")
    public ResponseEntity<List<String>> listUserUnityFiles(@RequestParam String userEmail) {
        try {
            List<String> files = s3Service.listUserUnityFiles(userEmail);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


}