package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.UserInfoResponseDTO;
import com.example.commerce.auth.entity.User;
import com.example.commerce.adminpanel.entity.UserProfile;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.exception.BusinessServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProfileService {

    private final UserRepository userRepository;

    /**
     * Token içerisindeki email bilgisiyle kullanıcının tüm profil detaylarını getirir.
     */
    @Transactional(readOnly = true)
    public UserInfoResponseDTO getAdminUserInfo(String email) {
        // 1. Kullanıcıyı email ile getir
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessServiceException("user.not.found", "Kullanıcı bulunamadı: " + email));

        // 2. UserProfile'a eriş (Lazy Loading olduğu için bu metot @Transactional olmalı)
        UserProfile profile = user.getUserProfile();

        // 3. Response DTO'yu inşa et
        // Profile null ise hata almamak için güvenli (safe-navigation) şekilde eşleme yapıyoruz
        return UserInfoResponseDTO.builder()
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(profile != null ? profile.getFirstName() : "")
                .lastName(profile != null ? profile.getLastName() : "")
                .phoneNumber(profile != null ? profile.getPhoneNumber() : "")
                .address(profile != null ? profile.getAddress() : "")
                .profilePicture(profile != null ? profile.getProfilePicture() : "")
                .description(profile != null ? profile.getDescription() : "")
                .build();
    }
}