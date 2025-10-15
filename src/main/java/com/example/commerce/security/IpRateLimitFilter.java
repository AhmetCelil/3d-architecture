package com.example.commerce.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class IpRateLimitFilter implements Filter {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    private Bucket resolveBucket(String ip) {
        return cache.get(ip, k -> {
            Refill refill = Refill.intervally(100, Duration.ofMinutes(1)); // 16 istek/dakika
            Bandwidth limit = Bandwidth.classic(18  , refill);
            return Bucket.builder().addLimit(limit).build();
        });
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader == null ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String ip = getClientIp(req);

        if (req.getRequestURI().contains("/login") || req.getRequestURI().contains("/register") || req.getRequestURI().contains("/s3")) {
            Bucket bucket = resolveBucket(ip);
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                ((HttpServletResponse) response).setStatus(429); // Too Many Requests
                response.getWriter().write("Çok fazla istek. Lütfen daha sonra tekrar deneyiniz.");
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
