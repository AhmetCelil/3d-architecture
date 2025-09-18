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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    // URL kullanım sayacı için memory cache
    private final Map<String, UrlUsageInfo> urlUsageCache = new ConcurrentHashMap<>();

    // URL kullanım bilgisi için inner class
    private static class UrlUsageInfo {
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
    }

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
     * 3 dakika geçerli ve maksimum 2 kez kullanılabilir presigned URL oluşturur
     *
     * @param fileName  Dosya adı
     * @param userEmail Kullanıcı email
     * @return Sınırlı kullanım bilgisi içeren response
     */
    public LimitedUrlResponse getUnityFileUrlWithLimits(String fileName, String userEmail) {
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

            // Eski expired URL'leri temizle
            cleanExpiredUrls();

            // 3 dakikalık presigned URL oluştur
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(3))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            // URL kullanım bilgisini cache'e kaydet
            String urlId = generateUrlId(objectKey);
            urlUsageCache.put(urlId, new UrlUsageInfo(2)); // Maksimum 2 kullanım

            System.out.println("✅ Sınırlı Presigned URL oluşturuldu: " + presignedUrl);

            return new LimitedUrlResponse(presignedUrl, urlId, 2, 3);

        } catch (Exception e) {
            System.out.println("❌ Hata: " + e.getMessage());
            throw new RuntimeException("S3'den dosya URL'si alınamadı: " + e.getMessage(), e);
        }
    }

    /**
     * URL kullanımını kontrol eder ve geçerli ise kullanım sayısını artırır
     *
     * @param urlId URL ID'si
     * @return URL kullanılabilir mi?
     */
    public boolean validateAndUseUrl(String urlId) {
        UrlUsageInfo usageInfo = urlUsageCache.get(urlId);

        if (usageInfo == null) {
            System.out.println("❌ URL ID bulunamadı: " + urlId);
            return false;
        }

        if (!usageInfo.canUse()) {
            if (usageInfo.isExpired()) {
                System.out.println("⏰ URL süresi doldu: " + urlId);
                urlUsageCache.remove(urlId); // Expired URL'yi temizle
            } else {
                System.out.println("🚫 URL kullanım limiti aşıldı: " + urlId +
                        " (Kalan: " + usageInfo.getRemainingUsage() + ")");
            }
            return false;
        }

        usageInfo.incrementUsage();
        System.out.println("✅ URL kullanıldı: " + urlId +
                " (Kalan kullanım: " + usageInfo.getRemainingUsage() + ")");
        return true;
    }

    /**
     * URL kullanım durumunu sorgular
     */
    public UrlStatusResponse getUrlStatus(String urlId) {
        UrlUsageInfo usageInfo = urlUsageCache.get(urlId);

        if (usageInfo == null) {
            return new UrlStatusResponse(false, 0, false, "URL bulunamadı");
        }

        return new UrlStatusResponse(
                usageInfo.canUse(),
                usageInfo.getRemainingUsage(),
                usageInfo.isExpired(),
                usageInfo.canUse() ? "Kullanılabilir" :
                        usageInfo.isExpired() ? "Süresi doldu" : "Kullanım limiti aşıldı"
        );
    }

    /**
     * Süresi dolmuş URL'leri temizler
     */
    private void cleanExpiredUrls() {
        urlUsageCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * URL için benzersiz ID oluşturur
     */
    private String generateUrlId(String objectKey) {
        return String.valueOf((objectKey + System.currentTimeMillis()).hashCode());
    }

    /**
     * Eski method - geriye dönük uyumluluk için (şimdi 3 dakika geçerli)
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

            // 3 dakikalık presigned URL oluştur
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(3))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            System.out.println("✅ Presigned URL oluşturuldu (3 dakika geçerli): " + presignedUrl);
            return presignedUrl;

        } catch (Exception e) {
            System.out.println("❌ Hata: " + e.getMessage());
            throw new RuntimeException("S3'den dosya URL'si alınamadı: " + e.getMessage(), e);
        }
    }

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