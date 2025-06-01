plugins {
    kotlin("jvm") version Version.kotlin
    kotlin("plugin.spring") version Version.kotlin
    kotlin("plugin.jpa") version Version.kotlin
    kotlin("plugin.noarg") version Version.kotlin
    kotlin("plugin.allopen") version Version.kotlin
    kotlin("kapt") version Version.kotlin
    id("org.springframework.boot") version Version.springBoot
    id("io.spring.dependency-management") version Version.springDependencyManagement
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

extra["springCloudVersion"] = "2024.0.1"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.oshai:kotlin-logging-jvm:${Version.kotlinLogging}")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")

    // Querydsl
    implementation("com.querydsl:querydsl-jpa:${Version.querydslJpa}")
    kapt("com.querydsl:querydsl-apt:${Version.querydslJpa}")
    implementation("com.vladmihalcea:hibernate-types-60:${Version.hibernateTypes60Version}")

    // Flyway + Database
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.postgresql:postgresql")

    // 카페인 캐시 (인메모리)
    implementation("com.github.ben-manes.caffeine:caffeine:${Version.caffeine}")

    // 형태소 분석기
    implementation("org.openkoreantext:open-korean-text:${Version.openKoreanText}")
    implementation("com.ibm.icu:icu4j:${Version.icu4j}")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${Version.kotlinTestJunit5}")
    testImplementation("io.kotest:kotest-assertions-core:${Version.kotestAssertionsCore}")
    testImplementation("io.kotest:kotest-runner-junit5:${Version.kotestAssertionsCore}")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:${Version.kotestExtensionsSpring}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.mockk:mockk:${Version.mockk}")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
    annotation("com.testgrafana.annotation.NoArgsConstructor")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
    annotation("com.testgrafana.annotation.NoArgsConstructor")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
springBoot {
    mainClass.set("com.testword.TestWordApplicationKt")
}
sourceSets {
    main {
        java {
            srcDirs("build/generated/source/kapt/main")
        }
    }
}
tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
}