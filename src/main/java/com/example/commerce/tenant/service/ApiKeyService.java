package com.example.commerce.tenant.service;

import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.tenant.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CompanyRepository companyRepository;

    /** Generates a new random raw API key. Only shown to the caller once. */
    public String generateRawKey() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Hashes a raw API key for storage/lookup. Raw keys are never persisted. */
    public String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Ham API key'i aktif şirkete çözer; geçersizse INVALID_API_KEY fırlatır. */
    public Company resolveCompany(String apiKey) {
        return companyRepository.findByApiKeyHashAndActiveTrue(hash(apiKey))
                .orElseThrow(() -> new BusinessServiceException("INVALID_API_KEY", "Geçersiz API Key"));
    }
}
