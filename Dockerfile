# Multi-stage Docker build for Spring Boot application
# Stage 1: Build with Maven
FROM maven:3.9.12-amazoncorretto-21 AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml first for better layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B && \
    mv target/*.jar app.jar

# Stage 2: Runtime image
FROM amazoncorretto:21

WORKDIR /app

# Copy the built jar
COPY --from=builder /build/app.jar app.jar

# Expose application port
EXPOSE 8012

# Run the Spring Boot application with optimized JVM settings
ENTRYPOINT ["java", \
     # G1GC (default in Java 21, optimized for low pause)
     "-XX:+UseG1GC", \
     # String deduplication to reduce memory
     "-XX:+UseStringDeduplication", \
     # Compressed oops for smaller memory footprint
     "-XX:+UseCompressedOops", \
     # Keep app alive for container health checks
     "-XX:+ExitOnOutOfMemoryError", \
     "-jar", "app.jar"]
