# ---- Build Stage ----
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY core ./core
COPY infra ./infra
COPY api-app ./api-app
COPY batch ./batch
RUN gradle :api-app:bootJar --no-daemon && \
    rm -f /app/api-app/build/libs/*-plain.jar

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/api-app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
