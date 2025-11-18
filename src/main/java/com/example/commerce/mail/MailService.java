/*
package com.example.commerce.mail;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.mail.dto.MailRequestDTO;
import com.example.commerce.mail.dto.MailResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.*;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    // API Key doğrulama
    private User getUserByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BusinessServiceException("INVALID_API_KEY", "Geçersiz API key"));
    }

    public MailResponseDTO mailGonder(String apiKey, MailRequestDTO requestDTO) {
        MailResponseDTO response = new MailResponseDTO();

        try {
            User user = getUserByApiKey(apiKey);


            String hedefMail = user.getEmail();

            String subject = "Yeni Proje İletişim Talebi - " + requestDTO.getIsim() + " " + requestDTO.getSoyisim();

            String body = """
                    Yeni bir proje iletişim talebi alındı.

                    Gönderen Bilgileri:
                    İsim: %s
                    Soyisim: %s
                    Telefon: %s
                    Email: %s

                    Açıklama:
                    %s

                    Gönderen Kullanıcı (API Key Sahibi):
                    %s
                    """.formatted(
                    requestDTO.getIsim(),
                    requestDTO.getSoyisim(),
                    requestDTO.getTelefon(),
                    requestDTO.getEmail(),
                    requestDTO.getAciklama(),
                    user.getEmail()
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(hedefMail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Mail gönderildi → Şirket Maili: {}, User Email: {}", hedefMail, user.getEmail());



        } catch (BusinessServiceException ex) {
            log.error("Mail gönderim hatası: {}", ex.getMessage());

        }

        return response;
    }
}
*/
