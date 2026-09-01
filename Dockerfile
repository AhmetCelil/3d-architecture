# ---- Build stage ----
FROM gradle:8.14.2-jdk17 AS build
WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts ./
COPY src src
RUN gradle bootJar --no-daemon -x test

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
