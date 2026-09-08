# syntax=docker/dockerfile:1
# 레이어 분리(jarmode tools extract)는 Spring Boot 공식 권장 형태를 따른다.
# https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html
# https://docs.spring.io/spring-boot/reference/packaging/container-images/efficient-images.html

# jar 을 어디서 가져올지 고른다. 기본은 이 안에서 직접 빌드하므로 깨끗한 체크아웃에서
# `docker build .` 이 그대로 된다. 호스트 배포는 --build-arg JAR_STAGE=prebuilt 로
# 미리 만든 jar 을 쓴다 — 그쪽만 Gradle 증분 컴파일을 살릴 수 있다.
# BuildKit 은 도달하지 않는 스테이지를 만들지 않으므로, 기본 경로에서 prebuilt 의
# COPY 는 실행되지 않는다(build/libs 가 없어도 된다).
ARG JAR_STAGE=source

FROM eclipse-temurin:21-jdk AS source
WORKDIR /src
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
# sharing=locked — gradle 이 캐시 디렉터리에 배타 락을 걸어 동시 빌드가 깨진다.
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew --no-daemon bootJar -x test && cp build/libs/*-SNAPSHOT.jar /app.jar

FROM eclipse-temurin:21-jdk AS prebuilt
COPY build/libs/*-SNAPSHOT.jar /app.jar

FROM ${JAR_STAGE} AS jar

FROM eclipse-temurin:21-jdk AS extract
WORKDIR /out
COPY --from=jar /app.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=extract /out/extracted/dependencies/ ./
COPY --from=extract /out/extracted/spring-boot-loader/ ./
COPY --from=extract /out/extracted/snapshot-dependencies/ ./
COPY --from=extract /out/extracted/application/ ./

# host-deploy.sh 가 의도한 커밋이 떴는지 이 값으로 확인한다.
ARG GIT_SHA=unknown
ENV GIT_SHA=${GIT_SHA}

# ⚠️ SPRING_PROFILES_ACTIVE 를 여기 굽지 말 것 — compose 가 런타임에 준다.
EXPOSE 8080
# exec 형식이라야 JVM 이 PID 1 이 된다. 쉘 형식으로 되돌리면 SIGTERM 이 sh 에서 멈춰
# graceful shutdown 없이 10초 뒤 SIGKILL 로 죽는다.
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
