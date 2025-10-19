plugins {
    java
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.xcodebn.demo"
version = "0.0.1-SNAPSHOT"

// Prevent this demo module from being published to Maven Central
tasks.withType<PublishToMavenRepository> {
    enabled = false
}

tasks.withType<PublishToMavenLocal> {
    enabled = false
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

repositories {
    mavenCentral()
}

dependencies {
    // Depend on the published zoun-admin-spring library from Maven Central
    implementation("io.github.xcodebn:zoun-admin-spring:0.0.2")

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // H2 in-memory database for demo
    runtimeOnly("com.h2database:h2")

    // Dev tools for hot reload
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
