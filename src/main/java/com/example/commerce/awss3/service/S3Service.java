package com.example.commerce.awss3.service;

import com.example.commerce.config.S3Configuration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class S3Service {

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private S3Configuration s3Configuration;

    /**
     * 15 dakika geçerli presigned URL oluşturur
     */
    public String getUnityFileUrl(String fileName, String userEmail) {
        // Önce bucket'taki tüm dosyaları listele
        System.out.println("🔍 Bucket içeriği kontrol ediliyor...");
        List<String> allFiles = listAllFiles();

        System.out.println("📦 Bucket'taki tüm dosyalar:");
        allFiles.forEach(file -> System.out.println("  - " + file));

        // Farklı path kombinasyonlarını dene
        String[] possiblePaths = {
                userEmail + "/" + fileName,           // acyalman@gmail.com/duvar.loader.js
                fileName,                              // duvar.loader.js (root'ta)
                "unity/" + fileName,                   // unity/duvar.loader.js
                "files/" + fileName,                   // files/duvar.loader.js
                userEmail.replace("@", "_") + "/" + fileName  // acyalman_gmail.com/duvar.loader.js
        };

        String objectKey = null;
        for (String path : possiblePaths) {
            System.out.println("🔎 Deneniyor: " + path);
            if (doesObjectExist(path)) {
                objectKey = path;
                System.out.println("✅ Dosya bulundu: " + path);
                break;
            }
        }

        if (objectKey == null) {
            throw new RuntimeException(
                    "Dosya hiçbir path'te bulunamadı: " + fileName +
                            "\nBucket'taki dosyalar: " + allFiles
            );
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Configuration.getBucketName())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(10))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        String url = presignedRequest.url().toString();
        System.out.println("📝 Generated URL: " + url);

        return url;
    }

    private boolean doesObjectExist(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Configuration.getBucketName())
                    .key(objectKey)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            System.out.println("⚠️ S3 Hatası (" + objectKey + "): " + e.awsErrorDetails().errorMessage());
            return false;
        } catch (Exception e) {
            System.out.println("⚠️ Genel hata (" + objectKey + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Bucket içindeki TÜM dosyaları listele
     */
    private List<String> listAllFiles() {
        List<String> files = new ArrayList<>();
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(s3Configuration.getBucketName())
                    .maxKeys(1000) // İlk 1000 dosya
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            listResponse.contents().forEach(obj -> {
                files.add(obj.key());
            });

        } catch (Exception e) {
            System.out.println("❌ Liste hatası: " + e.getMessage());
        }
        return files;
    }

    /**
     * Bucket içindeki dosyaları prefix ile listele
     */
    public void listBucketContents(String prefix) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(s3Configuration.getBucketName())
                    .prefix(prefix)
                    .maxKeys(100)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            System.out.println("📂 Bucket içeriği (prefix: " + prefix + "):");
            if (listResponse.contents().isEmpty()) {
                System.out.println("  (Boş)");
            } else {
                listResponse.contents().forEach(obj -> {
                    System.out.println("  - " + obj.key() + " (" + obj.size() + " bytes)");
                });
            }

        } catch (Exception e) {
            System.out.println("❌ Liste hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}