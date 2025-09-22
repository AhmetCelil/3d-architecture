package com.example.commerce.awss3.dto;

public class UrlStatusResponse {
    private boolean canUse;
    private int remainingUsage;
    private boolean expired;
    private String message;

    public UrlStatusResponse() {}

    public UrlStatusResponse(boolean canUse, int remainingUsage, boolean expired, String message) {
        this.canUse = canUse;
        this.remainingUsage = remainingUsage;
        this.expired = expired;
        this.message = message;
    }

    // Getters and Setters
    public boolean isCanUse() {
        return canUse;
    }

    public void setCanUse(boolean canUse) {
        this.canUse = canUse;
    }

    public int getRemainingUsage() {
        return remainingUsage;
    }

    public void setRemainingUsage(int remainingUsage) {
        this.remainingUsage = remainingUsage;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}