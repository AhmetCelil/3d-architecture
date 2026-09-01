package com.example.commerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Staging gibi arama motorlarına indekslenmemesi gereken ortamlarda tüm
 * yanıtlara X-Robots-Tag ekler. app.robots.no-index=true olduğunda devrede.
 */
@Component
public class NoIndexHeaderFilter extends OncePerRequestFilter {

    @Value("${app.robots.no-index:false}")
    private boolean noIndex;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (noIndex) {
            response.setHeader("X-Robots-Tag", "noindex, nofollow");
        }
        filterChain.doFilter(request, response);
    }
}
