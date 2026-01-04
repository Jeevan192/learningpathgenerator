# Stage 1: Build
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Run
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
# Create directory for H2 database
RUN mkdir -p /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
