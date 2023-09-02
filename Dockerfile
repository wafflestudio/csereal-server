FROM openjdk:17-slim

ARG PROFILE
ENV profile=$PROFILE

RUN mkdir /app
WORKDIR /app

COPY ./build/libs/*.jar /app/app.jar

RUN mkdir /app/cse-files

EXPOSE 8080

ENTRYPOINT java -Dspring.profiles.active=$profile -jar app.jar