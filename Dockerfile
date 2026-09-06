# syntax=docker/dockerfile:1
# 레이어 분리(jarmode tools extract)는 Spring Boot 공식 권장 형태를 따른다.
# https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html
# https://docs.spring.io/spring-boot/reference/packaging/container-images/efficient-images.html

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
# sharing=locked — gradle 이 캐시 디렉터리에 배타 락을 걸어 동시 빌드가 깨진다.
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew --no-daemon bootJar -x test && cp build/libs/*-SNAPSHOT.jar /app.jar

FROM eclipse-temurin:21-jdk AS extract
WORKDIR /out
COPY --from=build /app.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=extract /out/extracted/dependencies/ ./
COPY --from=extract /out/extracted/spring-boot-loader/ ./
COPY --from=extract /out/extracted/snapshot-dependencies/ ./
COPY --from=extract /out/extracted/application/ ./

ARG PROFILE
ENV SPRING_PROFILES_ACTIVE=${PROFILE}

EXPOSE 8080
# exec 형식이라야 JVM 이 PID 1 이 된다. 쉘 형식으로 되돌리면 SIGTERM 이 sh 에서 멈춰
# graceful shutdown 없이 10초 뒤 SIGKILL 로 죽는다.
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
