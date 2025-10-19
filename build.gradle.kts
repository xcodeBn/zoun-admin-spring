plugins {
    java
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.xcodebn"
version = "0.0.1"
description = "zoun-admin-spring"

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

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.0")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<GenerateModuleMetadata> {
    suppressedValidationErrors.add("dependencies-without-versions")
}

mavenPublishing {
    coordinates(
        groupId = "io.github.xcodebn",
        artifactId = "zoun-admin-spring",
        version = "0.0.1"
    )

    pom {
        name.set("Zoun Admin Spring")
        description.set("Auto-generated admin panel for Spring Boot applications with JPA entities")
        inceptionYear.set("2025")
        url.set("https://github.com/xcodebn/zoun-admin-spring")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("xcodebn")
                name.set("Hassan Bazzoun")
                email.set("hassan.bazzoundev@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/xcodebn/zoun-admin-spring")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}
