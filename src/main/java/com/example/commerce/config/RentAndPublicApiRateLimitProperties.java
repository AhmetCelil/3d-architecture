package com.example.commerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rate-limit.rent-and-public-api")
@Getter
@Setter
public class RentAndPublicApiRateLimitProperties {

    /** /api/public/** altındaki (rent public + publicapi) istekler için saatlik istek limiti. */
    private int requestsPerHour = 250;
}
