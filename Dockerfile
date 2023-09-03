FROM openjdk:17-slim

# Set profile
ARG PROFILE
ENV profile=$PROFILE

# Set workdir as /app
RUN mkdir /app
WORKDIR /app

# Copy jar file
COPY ./build/libs/*.jar /app/app.jar

# Make directories to mount
RUN mkdir /app/mainImage
RUN mkdir /app/attachment
RUN mkdir /app/cse-files

# Expose port 8080
EXPOSE 8080

ENTRYPOINT java -Dspring.profiles.active=$profile -jar app.jar