package com.example.commerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ÖNEMLI: allowCredentials = true iken "*" kullanılamaz
        // İki seçenek var:
        // 1) allowCredentials = false yapıp "*" kullan
        // 2) allowCredentials = true yapıp spesifik origin'ler belirt

        // Seçenek 1: Credentials olmadan (Önerilen - Unity için yeterli)
        config.setAllowCredentials(false); // false yapınca * kullanabiliriz
        config.setAllowedOrigins(Arrays.asList("*"));

        /* Seçenek 2: Credentials ile (Eğer cookie/auth gerekiyorsa)
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList("*")); // allowedOriginPatterns kullan
        // veya spesifik origin'ler:
        // config.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:3000",
        //     "http://localhost:5173",
        //     "https://yourdomain.com"
        // ));
        */

        // Tüm HTTP metodlarına izin ver
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // Tüm header'lara izin ver
        config.setAllowedHeaders(Arrays.asList("*"));

        // Expose headers (client'ın görebileceği header'lar)
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        // Preflight cache süresi (saniye)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Tüm /api/** endpoint'leri için geçerli
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}