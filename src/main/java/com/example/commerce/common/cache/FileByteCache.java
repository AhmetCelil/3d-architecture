package com.example.commerce.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Function;

/**
 * DB'de bytea olarak tutulan dosya/görsel içeriklerini tek instance'ın belleğinde
 * önbellekler; aynı dosya tekrar istendiğinde DB'ye gidip bytea'yı yeniden çekmeyi
 * (ve dolayısıyla DB network trafiğini) önler. Ağırlık byte cinsindendir, böylece
 * bellek kullanımı dosya sayısından değil toplam boyuttan sınırlanır.
 */
@Component
public class FileByteCache {

    private static final long MAX_WEIGHT_BYTES = 150L * 1024 * 1024;

    private final Cache<String, byte[]> cache = Caffeine.newBuilder()
            .maximumWeight(MAX_WEIGHT_BYTES)
            .weigher((String key, byte[] value) -> value.length)
            .expireAfterAccess(Duration.ofDays(30))
            .build();

    public byte[] get(String key, Function<String, byte[]> loader) {
        return cache.get(key, loader);
    }

    public void evict(String key) {
        cache.invalidate(key);
    }
}
