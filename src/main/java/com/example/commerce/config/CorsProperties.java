package com.example.commerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProperties {

    /** Explicit allow-list of origins permitted to make credentialed requests. No wildcards. */
    private List<String> allowedOrigins = new ArrayList<>();
}
