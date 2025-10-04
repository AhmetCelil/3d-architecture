package com.example.commerce.profil.service;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.profil.dto.*;
import com.example.commerce.profil.entity.CompanyProject;
import com.example.commerce.profil.entity.UserProfile;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfilServiceImpl {
    private final String MSG_SIFRE_DEGISTIRILDI = "Sifre Degistirildi";
    private final String MSG_PROFIL_BILGISI_AYARLANDI = "Profil Bilgisi olusturuldu/guncellendi.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public SirketSifreGuncelleResponseDTO sirketSifreGuncelleResponseDTO(SirketSifreGuncelleRequestDTO requestDTO) {
        SirketSifreGuncelleResponseDTO responseDTO = new SirketSifreGuncelleResponseDTO();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Kullanıcı doğrulanmamış.");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı"));

        String encodedPassword = passwordEncoder.encode(requestDTO.getYeniSirketParola());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_SIFRE_DEGISTIRILDI, AppMessageType.SUCCESS)
        ));

        return responseDTO;
    }
    public SirketProfilAyarlaResponseDTO sirketProfilAyarla(SirketProfilAyarlaRequestDTO requestDTO) {
        SirketProfilAyarlaResponseDTO responseDTO = new SirketProfilAyarlaResponseDTO();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessServiceException("kullanıcı bulunamadı","kullanıcı mevcut değil"));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setUserProfile(profile);
        }

        profile.setFirstName(requestDTO.getFirstName());
        profile.setLastName(requestDTO.getLastName());
        profile.setPhoneNumber(requestDTO.getPhoneNumber());
        profile.setAddress(requestDTO.getAddress());
        profile.setProfilePicture(requestDTO.getProfilePicture());
        profile.setDescription(requestDTO.getDescription());

        userRepository.save(user);

        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_PROFIL_BILGISI_AYARLANDI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    public SirketProjeAyarlaResponseDTO sirketProjesiAyarla(SirketProjeAyarlaRequestDTO requestDTO) {
        SirketProjeAyarlaResponseDTO responseDTO = new SirketProjeAyarlaResponseDTO();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessServiceException("kullanıcı bulunamadı", "kullanıcı mevcut değil"));

        CompanyProject project = CompanyProject.builder()
                .projectName(requestDTO.getProjectName())
                .description(requestDTO.getDescription())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .user(user)
                .build();

        // Eğer user.getProjects() null ise initialize et
        if (user.getProjects() == null) {
            user.setProjects(new ArrayList<>());
        }

        user.getProjects().add(project);
        userRepository.save(user);

        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode("MSG_PROJE_EKLEME_BASARILI", AppMessageType.SUCCESS)
        ));

        return responseDTO;
    }
}

