package com.example.commerce.util;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class FileResponseUtil {

    private FileResponseUtil() {
    }

    public static ResponseEntity<byte[]> inline(String fileName, String fileType, byte[] fileData) {
        return inline(fileName, fileType, fileData, null);
    }

    public static ResponseEntity<byte[]> inline(String fileName, String fileType, byte[] fileData, String cacheControl) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (fileType != null && !fileType.isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(fileType);
            } catch (IllegalArgumentException ignored) {
                // fileType beklenmeyen bir formatta ise octet-stream'e düş
            }
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(fileName).build().toString());
        if (cacheControl != null) {
            builder.header(HttpHeaders.CACHE_CONTROL, cacheControl);
        }
        return builder.body(fileData);
    }
}
