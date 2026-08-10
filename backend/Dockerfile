# Build the Spring Boot executable JAR with the project's Gradle wrapper.
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace/backend

COPY backend/gradlew gradlew
COPY backend/gradle gradle
COPY backend/build.gradle backend/settings.gradle ./
RUN chmod +x gradlew

COPY backend/src src
COPY database /workspace/database

RUN ./gradlew clean bootJar -x test --no-daemon \
    && JAR_FILE="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /tmp/app.jar

# Run with a smaller Java 21 image.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app

COPY --from=build --chown=app:app /tmp/app.jar /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
