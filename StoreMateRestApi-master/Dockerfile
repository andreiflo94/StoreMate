# ===== Build Stage =====
FROM gradle:8.7.0-jdk17-alpine AS build
WORKDIR /app

# Copiem fișierele de build Gradle pentru cache
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# Build dependințe (cache)
RUN ./gradlew build -x test --no-daemon || true

# Copiem sursa și generăm JAR-ul
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ===== Run Stage =====
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copiem JAR-ul din build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expunem portul aplicației
EXPOSE 8080

# Comanda pentru rulare
ENTRYPOINT ["java", "-jar", "app.jar"]
