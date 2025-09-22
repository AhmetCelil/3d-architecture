package com.example.commerce.awss3.dto;

public class LimitedUrlResponse {
    private String url;
    private String urlId;
    private int maxUsage;
    private int validityMinutes;

    public LimitedUrlResponse() {}

    public LimitedUrlResponse(String url, String urlId, int maxUsage, int validityMinutes) {
        this.url = url;
        this.urlId = urlId;
        this.maxUsage = maxUsage;
        this.validityMinutes = validityMinutes;
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlId() {
        return urlId;
    }

    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

    public int getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(int maxUsage) {
        this.maxUsage = maxUsage;
    }

    public int getValidityMinutes() {
        return validityMinutes;
    }

    public void setValidityMinutes(int validityMinutes) {
        this.validityMinutes = validityMinutes;
    }
}