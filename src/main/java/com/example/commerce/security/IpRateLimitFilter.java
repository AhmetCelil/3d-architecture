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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class IpRateLimitFilter extends OncePerRequestFilter implements Filter {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    private Bucket resolveBucket(String ip) {
        return cache.get(ip, k -> {
            Refill refill = Refill.intervally(1000, Duration.ofMinutes(1));
            Bandwidth limit = Bandwidth.classic(180  , refill);
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

        String ip = getClientIp(request);
        Bucket bucket = resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Çok fazla istek attınız (IP Limit). Lütfen daha sonra tekrar deneyiniz.");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean isRateLimitedPath = path.contains("/login")
                || path.contains("/register")
                || path.contains("/s3");

        return !isRateLimitedPath;
    }
}
