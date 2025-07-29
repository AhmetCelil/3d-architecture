package com.example.commerce.profil;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.auth.service.JwtService;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfilServiceImpl {
    private final String MSG_SIFRE_DEGISTIRILDI = "Sifre Degistirildi";

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
}

