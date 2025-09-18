package com.example.commerce.awss3;

public class UrlStatusResponse {
    private final boolean canUse;
    private final int remainingUsage;
    private final boolean expired;
    private final String message;

    public UrlStatusResponse(boolean canUse, int remainingUsage, boolean expired, String message) {
        this.canUse = canUse;
        this.remainingUsage = remainingUsage;
        this.expired = expired;
        this.message = message;
    }

    public boolean isCanUse() { return canUse; }
    public int getRemainingUsage() { return remainingUsage; }
    public boolean isExpired() { return expired; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("Kullanılabilir: %s\nKalan Kullanım: %d\nSüresi Doldu: %s\nMesaj: %s",
                canUse, remainingUsage, expired, message);
    }
}