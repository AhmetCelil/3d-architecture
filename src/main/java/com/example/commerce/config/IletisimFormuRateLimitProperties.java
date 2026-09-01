package com.example.commerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rate-limit.iletisim-formu")
@Getter
@Setter
public class IletisimFormuRateLimitProperties {

    /** POST /api/public/iletisim-formu için IP başına günlük istek limiti. */
    private int requestsPerDay = 4;
}
