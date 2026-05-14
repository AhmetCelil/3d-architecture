package com.example.commerce.altcha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Random;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AltchaService {

    @Value("${altcha.hmac-key:my-default-secret-key-for-altcha-12345}")
    private String hmacKey;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Replay protection: Aynı token sadece 1 kez kullanılabilir
    private final Cache<String, Boolean> usedSignatures = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    public AltchaChallengeDTO createChallenge() {
        String salt = HexFormat.of().formatHex(generateRandomBytes(12));
        int number = secureRandom.nextInt(100000, 1000000); // 100k to 1M range for difficulty
        
        String challenge = sha256(salt + number);
        String signature = hmacSha256(salt + challenge, hmacKey);

        return AltchaChallengeDTO.builder()
                .algorithm("SHA-256")
                .challenge(challenge)
                .salt(salt)
                .signature(signature)
                .build();
    }

    public boolean verify(String payloadBase64) {
        try {
            String json = new String(Base64.getDecoder().decode(payloadBase64), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(json, Map.class);

            String algorithm = (String) payload.get("algorithm");
            String challenge = (String) payload.get("challenge");
            String salt = (String) payload.get("salt");
            String signature = (String) payload.get("signature");
            Object numberObj = payload.get("number");
            
            if (numberObj == null) return false;
            String number = String.valueOf(numberObj);

            // 1. Verify Algorithm
            if (!"SHA-256".equals(algorithm)) return false;

            // 2. Replay Protection: Bu signature daha önce kullanıldı mı?
            if (usedSignatures.getIfPresent(signature) != null) {
                log.warn("Altcha replay attack detected: Signature already used");
                return false;
            }

            // 3. Verify Signature
            String expectedSignature = hmacSha256(salt + challenge, hmacKey);
            if (!expectedSignature.equals(signature)) {
                log.warn("Altcha signature mismatch");
                return false;
            }

            // 4. Verify Challenge (PoW)
            String expectedChallenge = sha256(salt + number);
            if (!expectedChallenge.equals(challenge)) {
                log.warn("Altcha challenge PoW failed");
                return false;
            }

            // Her şey doğruysa, bu imzayı "kullanıldı" olarak işaretle
            usedSignatures.put(signature, true);
            return true;
        } catch (Exception e) {
            log.error("Altcha verification error", e);
            return false;
        }
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String hmacSha256(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
