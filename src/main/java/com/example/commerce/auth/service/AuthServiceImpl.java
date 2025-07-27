package com.example.commerce.auth.service;

import com.example.commerce.auth.dto.UserLogInRequestDTO;
import com.example.commerce.auth.dto.UserLogInResponseDTO;
import com.example.commerce.auth.dto.UserRegisterRequestDTO;
import com.example.commerce.auth.dto.UserRegisterResponseDTO;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageDto;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.basedtos.BaseResponseDTO;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final String KULLANICI_KAYDEDILMISTIR = "Kullanıcı kaydedildi";
    private final String KULLANICI_KAYDI_MEVCUT = "Zaten kullanıcı kaydı bulunmaktadır";
    private final String KULLANICI_BULUNAMADI = "Kullanıcı bulunamadi";
    private final String KULLANICI_SIFRESI_YANLIS = "Kullanıcı şifresi yanlış";
    private final String SIFRE_YADA_MAIL_HATALI = "Şifre yada email hatalı";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserRegisterResponseDTO register(UserRegisterRequestDTO requestDTO) {

        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new BusinessServiceException("",KULLANICI_KAYDI_MEVCUT);
        }

        User user = User.builder()
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .role(requestDTO.getRole())
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());



        UserRegisterResponseDTO response = UserRegisterResponseDTO.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();

        response.setMessages(List.of(response.getMessages().toArray(new AppMessageDto[0]))); // 🟩 BaseResponseDto'dan gelen messages alanına mesaj ekleniyor

        return response;
    }


    @Override
    public UserLogInResponseDTO login(UserLogInRequestDTO requestDTO) {
        UserLogInResponseDTO responseDTO = new UserLogInResponseDTO();

        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElse(null);

        if (user == null) {
            throw new BusinessServiceException(SIFRE_YADA_MAIL_HATALI, "Kullanıcı bulunamadı: {0}", requestDTO.getEmail());

        }else if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
            throw new BusinessServiceException(KULLANICI_SIFRESI_YANLIS, "Kullanıcı bulunamadı: {0}", requestDTO.getEmail());

        }else{

            String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
            responseDTO.setToken(token);
            responseDTO.setEmail(user.getEmail());
            responseDTO.setRole(user.getRole());

            return responseDTO;
        }
    }
}
