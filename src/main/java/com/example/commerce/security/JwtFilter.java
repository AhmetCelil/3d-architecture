package com.example.commerce.security;

import com.example.commerce.auth.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Kullanıcı bazlı rate limit verileri
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                String role = jwtService.extractRole(jwt); // ADMIN, USER, SIRKET

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                // Rate limit kontrolü
                if (!checkRateLimit(username, role)) {
                    response.setStatus(429);
                    response.getWriter().write("Too many requests. Please try again later.");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    // Role göre rate limiti kontrol et
    private boolean checkRateLimit(String username, String role) {
        int maxRequestsPerMinute = switch (role.toUpperCase()) {
            case "ADMIN" -> 10;
            case "SIRKET" -> 5;
            case "USER" -> 4;
            default -> 10; // varsayılan
        };

        RateLimitInfo info = rateLimitMap.computeIfAbsent(username, k -> new RateLimitInfo());

        synchronized (info) {
            long currentTime = Instant.now().getEpochSecond();
            long currentMinute = currentTime / 60;

            if (info.lastCheckedMinute != currentMinute) {
                info.lastCheckedMinute = currentMinute;
                info.requestCount.set(0);
            }

            if (info.requestCount.incrementAndGet() > maxRequestsPerMinute) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/auth");
    }

    // İç sınıf: kullanıcı başına rate limit bilgisi
    private static class RateLimitInfo {
        long lastCheckedMinute;
        AtomicInteger requestCount = new AtomicInteger(0);
    }
}
