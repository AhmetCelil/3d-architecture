package com.example.commerce;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS için
public class ProxyController {

    @GetMapping("/proxy-download")
    public ResponseEntity<byte[]> proxyDownload(@RequestParam String url) {
        try {
            System.out.println("Proxy download için URL: " + url);

            // URL'yi doğrula (güvenlik için)
            if (!url.startsWith("http://localhost:9000/")) {
                System.out.println("Geçersiz URL: " + url);
                return ResponseEntity.badRequest().build();
            }

            RestTemplate restTemplate = new RestTemplate();

            // Signed URL'i doğrudan kullan (tekrar encode etme)
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/zip");
            headers.set("Access-Control-Allow-Origin", "*");
            headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.set("Access-Control-Allow-Headers", "*");

            System.out.println("ZIP başarıyla indirildi, boyut: " + response.getBody().length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response.getBody());

        } catch (Exception e) {
            System.err.println("Proxy download hatası: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }
}