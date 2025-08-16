// UnityService.java - Yeni Servis
package com.example.commerce.unity;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnityService {

    private final MinioClient minioClient;

    public Map<String, Object> analyzeUnityProject(String zipUrl) {
        log.info("Unity projesi analiz ediliyor: {}", zipUrl);

        try {
            // ZIP dosyasını indir
            Map<String, String> unityFiles = new HashMap<>();
            List<String> allFiles = new ArrayList<>();
            long totalSize = 0;

            try (InputStream zipStream = downloadZipFromUrl(zipUrl);
                 ZipInputStream zis = new ZipInputStream(zipStream)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        String fileName = entry.getName();
                        allFiles.add(fileName);
                        totalSize += entry.getSize();

                        // Unity dosyalarını kategorize et
                        if (fileName.endsWith(".data")) {
                            unityFiles.put("data", fileName);
                        } else if (fileName.endsWith(".wasm")) {
                            unityFiles.put("wasm", fileName);
                        } else if (fileName.contains("framework") && fileName.endsWith(".js")) {
                            unityFiles.put("framework", fileName);
                        } else if (fileName.contains("loader") && fileName.endsWith(".js")) {
                            unityFiles.put("loader", fileName);
                        } else if (fileName.endsWith(".json")) {
                            unityFiles.put("json", fileName);
                        } else if (fileName.endsWith(".html")) {
                            unityFiles.put("html", fileName);
                        }
                    }
                    zis.closeEntry();
                }
            }

            // Unity proje analizi
            boolean hasData = unityFiles.containsKey("data");
            boolean hasWasm = unityFiles.containsKey("wasm");
            boolean hasFramework = unityFiles.containsKey("framework");
            boolean isUnityProject = hasData && hasWasm;
            boolean canPlay = isUnityProject && (hasFramework || unityFiles.size() >= 3);

            log.info("Unity analizi tamamlandı - Unity: {}, Oynatılabilir: {}", isUnityProject, canPlay);

            return Map.of(
                    "isUnityProject", isUnityProject,
                    "canPlay", canPlay,
                    "files", unityFiles,
                    "totalFiles", allFiles.size(),
                    "totalSizeMB", totalSize / (1024 * 1024),
                    "fileList", allFiles.subList(0, Math.min(allFiles.size(), 20)), // İlk 20 dosya
                    "analysis", Map.of(
                            "hasData", hasData,
                            "hasWasm", hasWasm,
                            "hasFramework", hasFramework,
                            "hasLoader", unityFiles.containsKey("loader"),
                            "hasJson", unityFiles.containsKey("json"),
                            "hasHtml", unityFiles.containsKey("html")
                    )
            );

        } catch (Exception e) {
            log.error("Unity analiz hatası: {}", e.getMessage(), e);
            return Map.of(
                    "error", "Analiz hatası: " + e.getMessage(),
                    "canPlay", false,
                    "isUnityProject", false,
                    "files", Map.of()
            );
        }
    }

    private InputStream downloadZipFromUrl(String zipUrl) throws IOException {
        log.info("ZIP indiriliyor: {}", zipUrl);
        URL url = new URL(zipUrl);
        return url.openStream();
    }

    // Gelecekte kullanım için - ZIP içeriğini cache'leme
    public Map<String, Object> getUnityGameConfig(String zipUrl) {
        try {
            Map<String, Object> config = new HashMap<>();

            try (InputStream zipStream = downloadZipFromUrl(zipUrl);
                 ZipInputStream zis = new ZipInputStream(zipStream)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".json")) {
                        // Unity config JSON'ını oku
                        StringBuilder content = new StringBuilder();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            content.append(new String(buffer, 0, length));
                        }

                        // JSON parse et (basit implementation)
                        config.put("configJson", content.toString());
                        break;
                    }
                    zis.closeEntry();
                }
            }

            return config;
        } catch (Exception e) {
            log.error("Unity config okuma hatası: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}