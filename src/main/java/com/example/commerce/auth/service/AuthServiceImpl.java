package com.example.commerce.auth.service;

import com.example.commerce.auth.dto.*;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.AppException;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.security.JwtService;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String MSG_KULLANICI_KAYDEDILDI = "user.register.success";
    private static final String MSG_KULLANICI_KAYDI_MEVCUT = "user.register.exists";
    private static final String MSG_KULLANICI_BULUNAMADI = "user.login.notfound";
    private static final String MSG_SIFRE_YANLIS = "user.login.invalid.password";
    private static final String MSG_SIFRE_YA_DA_EMAIL_HATALI = "user.login.invalid.credentials";
    private static final String MSG_SIFRE_UZUNLUK_HATASI = "user.password.length.invalid";
    private static final String MSG_EMAIL_BOS = "user.email.empty";
    private static final String MSG_SIFRE_BOS = "user.password.empty";
    private static final String MSG_LOGIN_BEKLENMEYEN_HATA = "user.login.unexpected.error";
    private static final String MSG_REGISTER_BEKLENMEYEN_HATA = "user.register.failed";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserRegisterResponseDTO register(UserRegisterRequestDTO requestDTO) {
        UserRegisterResponseDTO responseDTO = new UserRegisterResponseDTO();

        try {
            if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
                throw new BusinessServiceException(MSG_KULLANICI_KAYDI_MEVCUT, "Zaten kullanıcı kaydı bulunmaktadır");
            }

            if (requestDTO.getPassword().length() < 4 || requestDTO.getPassword().length() > 40) {
                throw new ValidationServiceException(MSG_SIFRE_UZUNLUK_HATASI, "Şifre uzunluğu 4 ile 40 karakter arasında olmalıdır");
            }

            User user = User.builder()
                    .email(requestDTO.getEmail())
                    .password(passwordEncoder.encode(requestDTO.getPassword()))
                    .role(requestDTO.getRole())
                    .enabled(true)
                    .build();

            userRepository.save(user);

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_KULLANICI_KAYDEDILDI, AppMessageType.SUCCESS)
            ));
        }
        catch (BusinessServiceException | ValidationServiceException ex) {
            responseDTO.setMessages(List.of(AppMessageUtil.create(MSG_KULLANICI_KAYDI_MEVCUT, "Girilen mailin kaydı bulunmaktadır", AppMessageType.ERROR)));
        }
        catch (Exception ex) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(MSG_REGISTER_BEKLENMEYEN_HATA, "Kayıt sırasında beklenmeyen bir hata oluştu.", AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    @Override
    public UserLogInResponseDTO login(UserLogInRequestDTO requestDTO) {
        UserLogInResponseDTO responseDTO = new UserLogInResponseDTO();

        try {
            if (requestDTO.getEmail() == null || requestDTO.getEmail().isBlank()) {
                throw new ValidationServiceException(MSG_EMAIL_BOS, "E-posta boş olamaz");
            }

            if (requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
                throw new ValidationServiceException(MSG_SIFRE_BOS, "Şifre boş olamaz");
            }

            if (requestDTO.getPassword().length() < 4 || requestDTO.getPassword().length() > 40) {
                throw new ValidationServiceException(MSG_SIFRE_UZUNLUK_HATASI, "Şifre uzunluğu 4 ile 40 karakter arasında olmalıdır");
            }

            User user = userRepository.findByEmail(requestDTO.getEmail())
                    .orElseThrow(() -> new BusinessServiceException(MSG_KULLANICI_BULUNAMADI, "Kullanıcı bulunamadı: {0}", requestDTO.getEmail()));

            if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
                throw new BusinessServiceException(MSG_SIFRE_YANLIS, "Şifre hatalı: {0}", requestDTO.getEmail());
            }

            String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            responseDTO.setToken(accessToken);
            responseDTO.setRefreshToken(refreshToken);
            responseDTO.setEmail(user.getEmail());
            responseDTO.setRole(user.getRole());

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode("user.login.success", AppMessageType.SUCCESS)
            ));

            return responseDTO;

        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessServiceException(MSG_LOGIN_BEKLENMEYEN_HATA, "Beklenmeyen bir hata oluştu", ex.getMessage());
        }
    }

    @Override
    public UserLogOutResponseDTO logOut(UserLogOutRequestDTO requestDTO) {
        return null;
    }
}

