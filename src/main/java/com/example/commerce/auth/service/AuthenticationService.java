package com.example.commerce.auth.service;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.exception.BusinessServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String MSG_YETKISIZ_ERISIM = "yetkisiz.erisim";
    private static final String MSG_KULLANICI_BULUNAMADI = "kullanici.bulunamadi";

    private final UserRepository userRepository;

    /**
     * SecurityContext'ten authenticated user'ı alır
     * @return Authenticated User entity
     * @throws BusinessServiceException eğer user authenticated değilse veya bulunamazsa
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Yetkisiz erişim denemesi - Authentication null veya authenticated değil");
            throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Kullanıcı doğrulanmamış");
        }

        String email = authentication.getName();
        log.debug("Authenticated user email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User bulunamadı: {}", email);
                    return new BusinessServiceException(MSG_KULLANICI_BULUNAMADI, "Kullanıcı bulunamadı: " + email);
                });
    }

    /**
     * SecurityContext'ten authenticated user'ın email'ini alır
     * @return User email
     */
    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Kullanıcı doğrulanmamış");
        }

        return authentication.getName();
    }

    /**
     * SecurityContext'ten authenticated user'ın ID'sini alır
     * @return User ID
     */
    public Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }

    /**
     * Belirli bir user'ın authenticated user olup olmadığını kontrol eder
     * @param userId Kontrol edilecek user ID
     * @return true eğer authenticated user ise
     */
    public boolean isAuthenticatedUser(Long userId) {
        return getAuthenticatedUserId().equals(userId);
    }

    /**
     * Authenticated user'ın belirli bir role sahip olup olmadığını kontrol eder
     * @param role Kontrol edilecek rol
     * @return true eğer user rolü varsa
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}