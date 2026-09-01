package com.example.commerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Render (15 dk hareketsizlikte uyutuyor) ve Neon (5 dk'da compute'u sıfırlıyor)
 * free plan'ların ikisini de uyanık tutmak için kendi public URL'sine periyodik istek atar.
 * Sadece app.keep-alive.enabled=true olduğunda (staging) devrede.
 */
@Slf4j
@Component
public class StagingKeepAliveScheduler {

    @Value("${app.keep-alive.enabled:false}")
    private boolean enabled;

    @Value("${app.keep-alive.url:}")
    private String url;

    @Value("${app.keep-alive.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 10 * 60 * 1000, initialDelay = 10 * 60 * 1000)
    public void ping() {
        if (!enabled || url.isBlank()) {
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            if (!apiKey.isBlank()) {
                headers.set("X-API-Key", apiKey);
            }
            restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.info("[keep-alive] ping ok: {}", url);
        } catch (RestClientException ex) {
            log.warn("[keep-alive] ping failed: {} - {}", url, ex.getMessage());
        }
    }
}
