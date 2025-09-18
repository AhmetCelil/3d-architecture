package com.example.commerce.awss3;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitedUrlResponse {
    private final String url;
    private final String urlId;
    private final int maxUsage;
    private final int validityMinutes;

    public LimitedUrlResponse(String url, String urlId, int maxUsage, int validityMinutes) {
        this.url = url;
        this.urlId = urlId;
        this.maxUsage = maxUsage;
        this.validityMinutes = validityMinutes;
    }

    public String getUrl() { return url; }
    public String getUrlId() { return urlId; }
    public int getMaxUsage() { return maxUsage; }
    public int getValidityMinutes() { return validityMinutes; }

    @Override
    public String toString() {
        return String.format("URL: %s\nURL ID: %s\nMaksimum Kullanım: %d\nGeçerlilik: %d dakika",
                url, urlId, maxUsage, validityMinutes);
    }
}