package com.example.commerce.awss3.dto;

import java.time.LocalDateTime;

public class UrlUsageInfo {
    private int usageCount;
    private final LocalDateTime createdAt;
    private final int maxUsage;

    public UrlUsageInfo(int maxUsage) {
        this.usageCount = 0;
        this.createdAt = LocalDateTime.now();
        this.maxUsage = maxUsage;
    }

    public boolean canUse() {
        return usageCount < maxUsage && createdAt.plusMinutes(3).isAfter(LocalDateTime.now());
    }

    public void incrementUsage() {
        usageCount++;
    }

    public int getRemainingUsage() {
        return maxUsage - usageCount;
    }

    public boolean isExpired() {
        return createdAt.plusMinutes(3).isBefore(LocalDateTime.now());
    }

    // Getters
    public int getUsageCount() {
        return usageCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getMaxUsage() {
        return maxUsage;
    }
}