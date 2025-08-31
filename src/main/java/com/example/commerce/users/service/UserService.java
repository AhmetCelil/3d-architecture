package com.example.commerce.users.service;

import io.minio.MinioClient;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.BucketExistsArgs;
import io.minio.StatObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Value("${minio.accessKey:admin}")
    private String accessKey;

    @Value("${minio.secretKey:admin123}")
    private String secretKey;

    private final String BUCKET_NAME = "projects"; // "project" yerine "projects" kullanıyorsun

    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    // Test metodu
    public String testMinIOConnection() {
        try {
            MinioClient minioClient = getMinioClient();

            StringBuilder result = new StringBuilder();
            result.append("MinIO Bağlantı Testi:\n");
            result.append("Endpoint: ").append(minioEndpoint).append("\n");
            result.append("Access Key: ").append(accessKey).append("\n");

            // Bucket kontrolü
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(BUCKET_NAME).build()
            );
            result.append("Bucket '").append(BUCKET_NAME).append("' var mı: ").append(bucketExists).append("\n");

            if (bucketExists) {
                // Dosyaları listele
                result.append("\nBucket içeriği:\n");
                Iterable<Result<Item>> objects = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(BUCKET_NAME)
                                .prefix("ismail03@gmail.com/")
                                .build()
                );

                int fileCount = 0;
                for (Result<Item> itemResult : objects) {
                    Item item = itemResult.get();
                    result.append("- ").append(item.objectName()).append(" (").append(item.size()).append(" bytes)\n");
                    fileCount++;
                }
                result.append("Toplam dosya: ").append(fileCount);
            }

            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException("MinIO test hatası: " + e.getMessage());
        }
    }

    public String getUnityFileUrl(String fileName, String userEmail) {
        try {
            MinioClient minioClient = getMinioClient();

            // Debug için log'lar ekle
            System.out.println("🔍 MinIO Endpoint: " + minioEndpoint);
            System.out.println("🔍 Access Key: " + accessKey);
            System.out.println("🔍 Bucket: " + BUCKET_NAME);

            // Dosya yolu: projects/{userEmail}/{fileName}
            String objectName = userEmail + "/" + fileName;
            System.out.println("🔍 Object Name: " + objectName);

            // Bucket var mı kontrol et
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(BUCKET_NAME).build()
            );
            if (!bucketExists) {
                throw new RuntimeException("Bucket '" + BUCKET_NAME + "' bulunamadı!");
            }

            // Dosya var mı kontrol et
            try {
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(objectName)
                                .build()
                );
            } catch (Exception e) {
                throw new RuntimeException("Dosya bulunamadı: " + objectName);
            }

            // 1 saatlik erişim URL'si oluştur
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );

            System.out.println("✅ URL oluşturuldu: " + presignedUrl);
            return presignedUrl;

        } catch (Exception e) {
            System.out.println("❌ Hata: " + e.getMessage());
            throw new RuntimeException("MinIO'dan dosya URL'si alınamadı: " + e.getMessage());
        }
    }
}