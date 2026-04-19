package com.example.commerce.mail.service;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.mail.dto.MailRequestDTO;
import com.example.commerce.mail.dto.MailResponseDTO;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    @Value("${mail.brevo.api-key}")
    private String brevoApiKey;

    @Value("${mail.brevo.sender}")
    private String senderMail;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    // SSL kapatma işlemi (Kurumsal network kısıtlamaları için)
    static {
        disableSSLVerification();
    }

    private User getUserByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BusinessServiceException("INVALID_API_KEY", "Geçersiz API Key"));
    }

    public MailResponseDTO mailGonder(String apiKey, MailRequestDTO dto) {
        MailResponseDTO responseDTO = new MailResponseDTO();
        User user = getUserByApiKey(apiKey);

        try {
            // RestTemplate ve Timeout ayarları
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(10000);
            RestTemplate restTemplate = new RestTemplate(factory);

            // Headers - Brevo v3 standartlarına uygun
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("api-key", brevoApiKey.trim()); // x-sib-api-key yerine api-key kullanılır

            // Request Body
            Map<String, Object> requestBody = Map.of(
                    "sender", Map.of(
                            "name", "İnşaat Portfolyo Bildirimi",
                            "email", senderMail.trim()
                    ),
                    "to", List.of(Map.of(
                            "email", user.getEmail(),
                            "name", user.getEmail()
                    )),
                    "subject", "Yeni Proje İletişim Talebi",
                    "htmlContent", String.format("""
                    <html>
                      <body>
                        <h2 style='color: #2c3e50;'>Yeni İletişim Talebi</h2>
                        <p><b>Gönderen:</b> %s %s</p>
                        <p><b>Telefon:</b> %s</p>
                        <p><b>E-posta:</b> %s</p>
                        <br>
                        <p><b>Mesaj İçeriği:</b></p>
                        <div style='padding: 10px; background: #f9f9f9; border-left: 4px solid #3498db;'>
                          %s
                        </div>
                      </body>
                    </html>
                    """,
                            dto.getIsim(), dto.getSoyisim(), dto.getTelefon(),
                            dto.getEmail(), dto.getAciklama())
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Brevo API'ye mail isteği gönderiliyor... Hedef: {}", user.getEmail());

            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Mail başarıyla gönderildi. Brevo Response: {}", response.getBody());
                responseDTO.setSuccess(true);
                responseDTO.setMessages(List.of(
                        AppMessageUtil.createWithCode("Mesajınız başarıyla iletildi.", AppMessageType.SUCCESS)
                ));
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("BREVO 401 UNAUTHORIZED: API Key geçersiz veya süresi dolmuş! Body: {}", e.getResponseBodyAsString());
            responseDTO.setSuccess(false);
            responseDTO.setMessages(List.of(AppMessageUtil.createWithCode("Sistem yetkilendirme hatası (401).", AppMessageType.ERROR)));
        } catch (Exception e) {
            log.error("Mail gönderilirken teknik bir hata oluştu: ", e);
            responseDTO.setSuccess(false);
            responseDTO.setMessages(List.of(AppMessageUtil.createWithCode("Mail gönderimi başarısız oldu.", AppMessageType.ERROR)));
        }

        return responseDTO;
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