plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

// ✅ AWS SDK BOM - Bu kısım çok önemli!
dependencyManagement {
	imports {
		mavenBom("software.amazon.awssdk:bom:2.21.29")
	}
}

dependencies {
	// Test Dependencies
	testImplementation("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Spring Boot Dependencies
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")

	// Database
	runtimeOnly("org.postgresql:postgresql")

	// Liquibase
	implementation("org.liquibase:liquibase-core")

	// Rate Limiting
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")

	// Caching
	implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// MinIO (Legacy - silinebilir)
	implementation("io.minio:minio:8.5.2")

	// AWS S3 Dependencies - explicit versions required
	implementation("software.amazon.awssdk:s3:2.21.29")
	implementation("software.amazon.awssdk:auth:2.21.29")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}