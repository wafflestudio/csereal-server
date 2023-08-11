FROM openjdk:17-slim

ARG PROFILE=prod

RUN mkdir /app
WORKDIR /app

COPY ./build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT java -Dspring.profiles.active=$PROFILE -jar app.jar