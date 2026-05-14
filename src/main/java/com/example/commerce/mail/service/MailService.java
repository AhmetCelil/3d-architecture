package com.example.commerce.mail.service;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.mail.dto.MailRequestDTO;
import com.example.commerce.mail.dto.MailResponseDTO;
import com.example.commerce.util.AppMessageUtil;
import com.example.commerce.altcha.AltchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final UserRepository userRepository;
    private final AltchaService altchaService;

    @Value("${mail.brevo.api-key}")
    private String brevoApiKey;

    @Value("${mail.brevo.sender}")
    private String senderMail;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    // SSL kapatma işlemi (Kurumsal network kısıtlamaları için)
    static {
        disableSSLVerification();
    }

    public MailResponseDTO mailGonder(String apiKey, MailRequestDTO dto) {
        // 1. CAPTCHA DOĞRULAMA (PoW Check)
        if (!verifyCaptcha(dto.getCaptchaToken())) {
            throw new BusinessServiceException("INVALID_CAPTCHA", "Güvenlik doğrulaması geçilemedi.");
        }

        MailResponseDTO responseDTO = new MailResponseDTO();
        User user = getUserByApiKey(apiKey);

        try {
            RestTemplate restTemplate = new RestTemplate(getFactory());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey.trim());

            Map<String, Object> requestBody = Map.of(
                    "sender", Map.of("name", "İnşaat Portfolyo Bildirimi", "email", senderMail.trim()),
                    "to", List.of(Map.of("email", user.getEmail(), "name", user.getEmail())),
                    "subject", "Yeni Proje İletişim Talebi",
                    "htmlContent", buildHtml(dto)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                responseDTO.setSuccess(true);
                responseDTO.setMessages(List.of(AppMessageUtil.createWithCode("Başarılı", AppMessageType.SUCCESS)));
            }
        } catch (Exception e) {
            log.error("Mail hatası: ", e);
            responseDTO.setSuccess(false);
        }
        return responseDTO;
    }

    private boolean verifyCaptcha(String payload) {
        return altchaService.verify(payload);
    }
    private String buildHtml(MailRequestDTO dto) {
        return String.format("<html><body><h2>Yeni Talep</h2><p>%s %s</p><p>%s</p></body></html>",
                dto.getIsim(), dto.getSoyisim(), dto.getAciklama());
    }

    private SimpleClientHttpRequestFactory getFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return factory;
    }

    private User getUserByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BusinessServiceException("INVALID_API_KEY", "Geçersiz API Key"));
    }

    /**
     * Kurumsal networklerdeki SSL (Handshake/Revocation) hatalarını bypass etmek için.
     * Production ortamında güvenli sertifika yönetimi yapılmalıdır.
     */
    private static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            log.warn("!!! SSL Doğrulaması Devre Dışı Bırakıldı (Development Modu) !!!");
        } catch (Exception e) {
            log.error("SSL Bypass hatası: ", e);
        }
    }
}