FROM openjdk:17-slim

ARG PROFILE
ENV profile=$PROFILE

RUN mkdir /app
WORKDIR /app

COPY ./build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT java -Dspring.profiles.active=$profile -jar app.jar