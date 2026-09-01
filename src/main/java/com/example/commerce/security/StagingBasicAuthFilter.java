package com.example.commerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Staging'i arama motorlarına/botlara/rastgele ziyaretçilere kapatmak için tüm isteklerin
 * önüne konan blanket HTTP Basic Auth kapısı. Uygulamanın kendi JWT auth'undan bağımsızdır;
 * sadece app.basic-auth.enabled=true olduğunda (staging profili) devrede.
 */
@Component
public class StagingBasicAuthFilter extends OncePerRequestFilter {

    @Value("${app.basic-auth.enabled:false}")
    private boolean enabled;

    @Value("${app.basic-auth.username:}")
    private String username;

    @Value("${app.basic-auth.password:}")
    private String password;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled || isValid(request.getHeader(HttpHeaders.AUTHORIZATION))) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Staging\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private boolean isValid(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            return false;
        }

        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(authorizationHeader.substring("Basic ".length())), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return false;
        }

        int separatorIndex = decoded.indexOf(':');
        if (separatorIndex < 0) {
            return false;
        }

        String suppliedUsername = decoded.substring(0, separatorIndex);
        String suppliedPassword = decoded.substring(separatorIndex + 1);

        return constantTimeEquals(suppliedUsername, username) && constantTimeEquals(suppliedPassword, password);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
