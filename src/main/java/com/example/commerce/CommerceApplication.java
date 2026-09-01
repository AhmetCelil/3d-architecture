package com.example.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommerceApplication {

	public static void main(String[] args) {
		// TEMP DEBUG: Render'da env var'ların gerçekten ulaşıp ulaşmadığını teşhis etmek için.
		// Değerleri değil, sadece var/yok bilgisini loglar. Sorun çözülünce kaldırılacak.
		for (String key : new String[]{"DB_URL", "DB_USERNAME", "DB_PASSWORD", "SPRING_PROFILES_ACTIVE", "PORT"}) {
			String value = System.getenv(key);
			System.out.println("[ENV-DEBUG] " + key + " present=" + (value != null && !value.isBlank())
					+ " length=" + (value == null ? 0 : value.length()));
		}
		SpringApplication.run(CommerceApplication.class, args);
	}
}

