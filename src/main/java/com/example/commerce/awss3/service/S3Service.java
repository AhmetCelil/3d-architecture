package com.example.commerce.awss3.service;

import com.example.commerce.config.S3Configuration;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
@AllArgsConstructor
public class S3Service {

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private S3Configuration s3Configuration;

    /**
     * 5 saniye geçerli presigned URL oluşturur
     */
    public String getUnityFileUrl(String fileName, String userEmail) {
        try {
            // Debug logları
            System.out.println("🔍 S3 Region: " + s3Configuration.getRegion());
            System.out.println("🔍 S3 Bucket: " + s3Configuration.getBucketName());

            // Dosya yolu: {userEmail}/{fileName}
            String objectKey = userEmail + "/" + fileName;
            System.out.println("🔍 Object Key: " + objectKey);

            // Dosya varlığını kontrol et
            if (!doesObjectExist(objectKey)) {
                throw new RuntimeException("Dosya bulunamadı: " + objectKey);
            }

            // 5 saniyelik presigned URL oluştur
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Configuration.getBucketName())
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(5))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            System.out.println("✅ Presigned URL oluşturuldu (5 saniye geçerli): " + presignedUrl);
            return presignedUrl;

        } catch (Exception e) {
            System.out.println("❌ Hata: " + e.getMessage());
            throw new RuntimeException("S3'den dosya URL'si alınamadı: " + e.getMessage(), e);
        }
    }

    private boolean doesObjectExist(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Configuration.getBucketName())
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            System.out.println("⚠️ Dosya kontrol hatası: " + e.getMessage());
            return false;
        }
    }
}