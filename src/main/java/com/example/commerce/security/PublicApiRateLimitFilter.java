package com.example.commerce.security;

import com.example.commerce.config.RentAndPublicApiRateLimitProperties;
import com.example.commerce.tenant.service.ApiKeyService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * /api/public/** için IP bazlı genel limitten (IpRateLimitFilter) ayrı, daha
 * sıkı bir limit uygular: API key'e sahip partner istekleri key başına,
 * key olmayan (unique-code) istekler ise IP başına - böylece bir tarama
 * script'i tek bir key ile tüm IP limitini tüketip diğer partnerleri
 * etkileyemez ve unique-code brute force'u ayrı/sıkı bir limitle yavaşlatılır.
 * Limit (istek/saat) app.rate-limit.rent-and-public-api.requests-per-hour ile ayarlanır.
 */
@Component
@RequiredArgsConstructor
public class PublicApiRateLimitFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;
    private final RentAndPublicApiRateLimitProperties rateLimitProperties;

    private final Cache<String, Bucket> apiKeyBuckets = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(5000)
            .build();

    private final Cache<String, Bucket> codeLookupBuckets = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(5000)
            .build();

    private Bucket resolveApiKeyBucket(String keyHash) {
        return apiKeyBuckets.get(keyHash, k -> newHourlyBucket());
    }

    private Bucket resolveCodeLookupBucket(String ip) {
        return codeLookupBuckets.get(ip, k -> newHourlyBucket());
    }

    private Bucket newHourlyBucket() {
        int requestsPerHour = rateLimitProperties.getRequestsPerHour();
        Refill refill = Refill.intervally(requestsPerHour, Duration.ofHours(1));
        Bandwidth limit = Bandwidth.classic(requestsPerHour, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader == null ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");
        Bucket bucket = (apiKey != null && !apiKey.isBlank())
                ? resolveApiKeyBucket(apiKeyService.hash(apiKey))
                : resolveCodeLookupBucket(getClientIp(request));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Çok fazla istek. Lütfen daha sonra tekrar deneyiniz.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().contains("/api/public");
    }
}
