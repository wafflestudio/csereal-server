import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    kotlin("kapt") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("org.flywaydb.flyway") version "10.20.1"
}

group = "com.wafflestudio"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    // flyway
    implementation("org.flywaydb:flyway-core:10.20.1")
    implementation("org.flywaydb:flyway-mysql:10.20.1")

    // kotest
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.8.1")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.1")
    testImplementation("io.kotest:kotest-property-jvm:5.8.1")
    implementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    testImplementation("org.testcontainers:testcontainers:1.20.6")
    testImplementation("org.testcontainers:junit-jupiter:1.20.6")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.3.4")
    testImplementation("org.testcontainers:mysql:1.20.6")

    // mockk
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")

    // h2 database
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("com.h2database:h2")

    // queryDsl
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")

    // slack log appender
    implementation("com.cyfrania:logback-slack-appender:1.2")

    // 태그 제거
    implementation("org.jsoup:jsoup:1.17.2")

    // 이미지 업로드
    implementation("commons-io:commons-io:2.11.0")

    // Custom Metadata
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-mysql:10.20.1")
        classpath("com.mysql:mysql-connector-j:8.3.0")
    }
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

apply {
    plugin("org.jlleitschuh.gradle.ktlint")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
}

flyway {
    url = "jdbc:mysql://127.0.0.1:3306/csereal?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul"
    user = "root"
    password = "password"
    locations = arrayOf("classpath:db/migration")
}
