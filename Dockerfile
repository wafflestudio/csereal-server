FROM openjdk:18-slim

ARG PROFILE=prod

RUN mkdir /app
WORKDIR /app

COPY ./build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "-Dspring.profiles.active=${PROFILE}"]