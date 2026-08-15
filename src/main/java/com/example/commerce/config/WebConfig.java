package com.example.commerce.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class WebConfig {

    /**
     * Multipart dosya yükleme ayarları.
     * Bu bean tanımlı olduğu için Spring Boot'un application-local.yml'deki
     * spring.servlet.multipart.max-file-size/max-request-size (200MB) ayarlarını
     * otomatik uygulaması devre dışı kalır (@ConditionalOnMissingBean) — limitler
     * burada yml ile tutarlı olacak şekilde ayarlanmalı.
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(200));
        factory.setMaxRequestSize(DataSize.ofMegabytes(200));
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));
        return factory.createMultipartConfig();
    }

    /**
     * Multipart Resolver
     */
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(true);
        return resolver;
    }

    /**
     * Tomcat yapılandırması - File count limit çözümü
     */
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        factory.addConnectorCustomizers((Connector connector) -> {
            // Maksimum parametre sayısı
            connector.setProperty("maxParameterCount", "50000");

            // Maksimum POST boyutu
            connector.setMaxPostSize(104857600); // 100MB

            if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                AbstractHttp11Protocol<?> protocol = (AbstractHttp11Protocol<?>) connector.getProtocolHandler();
                protocol.setMaxHttpHeaderSize(65536); // 64KB
                protocol.setConnectionTimeout(120000); // 120 saniye

                // 🔽 Önemli kısım: Dosya sayısı limitini artır
                protocol.setProperty("fileCountMax", "1000");
            }
        });

        return factory;
    }


    /**
     * ObjectMapper bean - JSON deserialization için
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}