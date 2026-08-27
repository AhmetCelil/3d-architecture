package com.example.commerce.whatsapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
public class WhatsAppService {

    private static final String CALLMEBOT_URL = "https://api.callmebot.com/whatsapp.php";

    /**
     * CallMeBot üzerinden WhatsApp mesajı gönderir. Şirket henüz whatsappNumber/whatsappApiKey
     * girmediyse (admin panel iletişim bilgileri) sessizce atlanır; bu bildirim tamamen opsiyoneldir.
     */
    public boolean sendMessage(String phone, String apiKey, String text) {
        if (phone == null || phone.isBlank() || apiKey == null || apiKey.isBlank()) {
            return false;
        }
        try {
            // getForEntity(String, ...) URL'i bir template olarak görüp yeniden encode ediyor;
            // burada zaten encode edilmiş bir URI nesnesi verip çifte encode'u (%20 -> %2520) önlüyoruz.
            URI uri = UriComponentsBuilder.fromUriString(CALLMEBOT_URL)
                    .queryParam("phone", phone.trim())
                    .queryParam("text", text)
                    .queryParam("apikey", apiKey.trim())
                    .build()
                    .encode()
                    .toUri();

            ResponseEntity<String> response = new RestTemplate().getForEntity(uri, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("WhatsApp mesajı gönderilemedi: ", e);
            return false;
        }
    }
}
