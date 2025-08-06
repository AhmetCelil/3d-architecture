# Dockerfile

# Java imajını kullan
FROM openjdk:17-jdk-slim

# Uygulama jar dosyasını konteyner içine kopyala
COPY build/libs/commerce-0.0.1-SNAPSHOT.jar app.jar

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"]
