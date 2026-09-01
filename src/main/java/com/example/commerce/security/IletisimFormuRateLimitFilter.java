package com.example.commerce.security;

import com.example.commerce.config.IletisimFormuRateLimitProperties;
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
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * İletişim formu (POST /api/public/iletisim-formu) spam/otomasyon ile
 * doldurulup şirkete gereksiz WhatsApp/DB kaydı oluşturmasın diye, genel
 * PublicApiRateLimitFilter'dan bağımsız, IP başına günlük ayrı bir limit uygular.
 */
@Component
@RequiredArgsConstructor
public class IletisimFormuRateLimitFilter extends OncePerRequestFilter {

    private static final String PATH = "/api/public/iletisim-formu";

    private final IletisimFormuRateLimitProperties rateLimitProperties;

    private final Cache<String, Bucket> ipBuckets = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(5000)
            .build();

    private Bucket resolveBucket(String ip) {
        return ipBuckets.get(ip, k -> {
            int requestsPerDay = rateLimitProperties.getRequestsPerDay();
            Refill refill = Refill.intervally(requestsPerDay, Duration.ofDays(1));
            Bandwidth limit = Bandwidth.classic(requestsPerDay, refill);
            return Bucket.builder().addLimit(limit).build();
        });
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader == null ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (resolveBucket(getClientIp(request)).tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Çok fazla istek. Lütfen daha sonra tekrar deneyiniz.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(request.getRequestURI().endsWith(PATH) && HttpMethod.POST.matches(request.getMethod()));
    }
}
