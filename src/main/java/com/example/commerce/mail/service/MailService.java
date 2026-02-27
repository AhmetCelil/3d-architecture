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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
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

    @Value("${mail.brevo.api-key}")
    private String brevoApiKey;

    @Value("${mail.brevo.sender}")
    private String senderMail;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    // SSL yapılandırması sadece bir kere yapılsın
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
            RestTemplate restTemplate = new RestTemplateBuilder()
                    .requestFactory(this::createRequestFactory)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.set("accept", "application/json");
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "sender", Map.of(
                            "name", "Proje İletişim",
                            "email", senderMail
                    ),
                    "to", List.of(Map.of(
                            "email", user.getEmail(),
                            "name", user.getEmail()
                    )),
                    "subject", "Yeni Proje İletişim Talebi",
                    "htmlContent", String.format("""
                    <html>
                      <body>
                        <h3>Proje İletişim Talebi</h3>
                        <p><b>İsim:</b> %s</p>
                        <p><b>Soyisim:</b> %s</p>
                        <p><b>Telefon:</b> %s</p>
                        <p><b>Email:</b> %s</p>
                        <p><b>Açıklama:</b></p>
                        <p>%s</p>
                        <hr>
                      </body>
                    </html>
                    """,
                            dto.getIsim(), dto.getSoyisim(), dto.getTelefon(),
                            dto.getEmail(), dto.getAciklama()
                    )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);

            log.info("Mail gönderildi: {}", response.getStatusCode());

            responseDTO.setSuccess(true);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode("Mailiniz Firmaya Gönderilmiştir.", AppMessageType.SUCCESS)
            ));
            return responseDTO;
        } catch (Exception e) {
            responseDTO.setSuccess(true);
            responseDTO.setMessages(List.of(AppMessageUtil.createWithCode("Mail gönderilemedi.", AppMessageType.SUCCESS)));
            return responseDTO;
        }
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return factory;
    }

    /**
     * SSL sertifika doğrulamasını devre dışı bırak
     * ⚠️ SADECE DEVELOPMENT ORTAMI İÇİN!
     * Production'da gerçek sertifikalar kullanılmalıdır.
     */
    private static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            System.out.println("⚠️ SSL doğrulaması devre dışı (Development modu)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}