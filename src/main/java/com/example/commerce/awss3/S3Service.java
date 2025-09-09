package com.example.commerce.awss3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3Service {

    @Value("${aws.s3.accessKey}")
    private String accessKey;

    @Value("${aws.s3.secretKey}")
    private String secretKey;

    @Value("${aws.s3.region:eu-north-1}")
    private String region;

    @Value("${aws.s3.bucketName:projects3d}")
    private String bucketName;

    private S3Client getS3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    private S3Presigner getS3Presigner() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    /**
     * S3 bağlantısını test eder ve bucket içeriğini listeler
     */
    public String testS3Connection() {
        try (S3Client s3Client = getS3Client()) {
            StringBuilder result = new StringBuilder();
            result.append("Amazon S3 Bağlantı Testi:\n");
            result.append("Region: ").append(region).append("\n");
            result.append("Bucket: ").append(bucketName).append("\n");
            result.append("Access Key: ").append(accessKey.substring(0, 8)).append("...\n");

            // Bucket varlığını kontrol et
            try {
                HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.headBucket(headBucketRequest);
                result.append("Bucket '").append(bucketName).append("' erişilebilir: ✅\n");
            } catch (NoSuchBucketException e) {
                result.append("Bucket '").append(bucketName).append("' bulunamadı: ❌\n");
                return result.toString();
            } catch (Exception e) {
                result.append("Bucket erişim hatası: ❌ ").append(e.getMessage()).append("\n");
                return result.toString();
            }

            // Belirli kullanıcının dosyalarını listele (test için)
            String testUserEmail = "ismail03@gmail.com";
            result.append("\nTest kullanıcısı (").append(testUserEmail).append(") dosyaları:\n");

            try {
                ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(testUserEmail + "/")
                        .build();

                ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
                List<S3Object> objects = listResponse.contents();

                if (objects.isEmpty()) {
                    result.append("Dosya bulunamadı\n");
                } else {
                    for (S3Object object : objects) {
                        result.append("- ").append(object.key())
                                .append(" (").append(object.size()).append(" bytes)\n");
                    }
                    result.append("Toplam dosya: ").append(objects.size());
                }
            } catch (Exception e) {
                result.append("Dosya listesi alınamadı: ").append(e.getMessage()).append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException("S3 test hatası: " + e.getMessage(), e);
        }
    }

    /**
     * Unity dosyasının presigned URL'sini oluşturur
     */
    public String getUnityFileUrl(String fileName, String userEmail) {
        try (S3Presigner presigner = getS3Presigner()) {

            // Debug logları
            System.out.println("🔍 S3 Region: " + region);
            System.out.println("🔍 S3 Bucket: " + bucketName);

            // Dosya yolu: {userEmail}/{fileName}
            String objectKey = userEmail + "/" + fileName;
            System.out.println("🔍 Object Key: " + objectKey);

            // Dosya varlığını kontrol et
            if (!doesObjectExist(objectKey)) {
                throw new RuntimeException("Dosya bulunamadı: " + objectKey);
            }

            // 1 saatlik presigned URL oluştur
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            // ✅ DOĞRU METHOD ADI: presignGetObject (presignGetObjectRequest değil!)
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            System.out.println("✅ Presigned URL oluşturuldu: " + presignedUrl);
            return presignedUrl;

        } catch (Exception e) {
            System.out.println("❌ Hata: " + e.getMessage());
            throw new RuntimeException("S3'den dosya URL'si alınamadı: " + e.getMessage(), e);
        }
    }

    /**
     * Direkt S3 URL'sini döndürür (public read access gerekli)
     */
    public String getDirectS3Url(String fileName, String userEmail) {
        String objectKey = userEmail + "/" + fileName;

        // Dosya varlığını kontrol et
        if (!doesObjectExist(objectKey)) {
            throw new RuntimeException("Dosya bulunamadı: " + objectKey);
        }

        // Direkt S3 URL'si oluştur - URL encoding için %40 yerine @ kullan
        String encodedObjectKey = objectKey.replace("@", "%40");
        String directUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, encodedObjectKey);

        System.out.println("✅ Direkt S3 URL: " + directUrl);
        return directUrl;
    }

    /**
     * Kullanıcının Unity dosyalarını listeler
     */
    public List<String> listUserUnityFiles(String userEmail) {
        try (S3Client s3Client = getS3Client()) {

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(userEmail + "/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            return listResponse.contents().stream()
                    .map(S3Object::key)
                    .map(key -> key.substring(key.lastIndexOf("/") + 1)) // Sadece dosya adını al
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Kullanıcı dosyaları listelenemedi: " + e.getMessage(), e);
        }
    }

    /**
     * S3'de nesnenin var olup olmadığını kontrol eder
     */
    private boolean doesObjectExist(String objectKey) {
        try (S3Client s3Client = getS3Client()) {

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
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