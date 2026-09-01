package com.example.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

@SpringBootApplication
public class CommerceApplication {

	public static void main(String[] args) {
		// TEMP DEBUG: application-staging.yml runtime classpath'te gerçekten var mı,
		// ve SPRING_CONFIG_* gibi bir env var config lookup'ını saptırıyor mu.
		ClassLoader cl = CommerceApplication.class.getClassLoader();
		System.out.println("[ENV-DEBUG3] getResource(application-staging.yml)=" + cl.getResource("application-staging.yml"));
		System.out.println("[ENV-DEBUG3] getResource(application.yml)=" + cl.getResource("application.yml"));
		for (String key : new String[]{"SPRING_CONFIG_LOCATION", "SPRING_CONFIG_NAME", "SPRING_CONFIG_ADDITIONAL_LOCATION", "JAVA_TOOL_OPTIONS", "JAVA_OPTS"}) {
			System.out.println("[ENV-DEBUG3] " + key + "=" + System.getenv(key));
		}
		System.out.println("[ENV-DEBUG3] all env var keys: " + String.join(", ", System.getenv().keySet()));

		SpringApplication app = new SpringApplication(CommerceApplication.class);
		// TEMP DEBUG: Spring'in kendi Environment'ının spring.datasource.url'i gerçekten
		// çözüp çözemediğini teşhis etmek için. Değerleri değil, sadece durum bilgisini loglar.
		app.addListeners((ApplicationListener<ApplicationPreparedEvent>) event -> {
			Environment env = event.getApplicationContext().getEnvironment();
			String url = env.getProperty("spring.datasource.url");
			System.out.println("[ENV-DEBUG2] active profiles: " + String.join(",", env.getActiveProfiles()));
			System.out.println("[ENV-DEBUG2] spring.datasource.url present=" + (url != null)
					+ " length=" + (url == null ? -1 : url.length()));
			System.out.println("[ENV-DEBUG2] env.getProperty(DB_URL) present=" + (env.getProperty("DB_URL") != null));
			MutablePropertySources sources = ((org.springframework.core.env.ConfigurableEnvironment) env).getPropertySources();
			for (PropertySource<?> source : sources) {
				System.out.println("[ENV-DEBUG2] property source: " + source.getName());
			}
		});
		app.run(args);
	}
}

