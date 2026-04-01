# ---- Build Stage ----
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY core ./core
COPY infra ./infra
COPY batch ./batch
RUN gradle :batch:bootJar --no-daemon -x test && \
    rm -f /app/batch/build/libs/*-plain.jar

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/batch/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
