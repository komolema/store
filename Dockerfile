# Multi-stage Dockerfile for the Spring Boot application
# - Build stage: uses the Gradle wrapper to produce a fat jar (bootJar)
# - Runtime stage: small JRE image, non-root user, healthcheck and sensible defaults

#############################
# Build stage
#############################
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Allow Docker layer caching of Gradle dependencies where possible
COPY gradle gradle
COPY gradlew .
COPY build.gradle settings.gradle gradle.properties* ./
COPY settings.gradle ./
RUN chmod +x ./gradlew

# Copy the rest of the sources
COPY . .

# Build the application (skip tests in image builds to speed up) - CI should run tests separately
RUN ./gradlew clean bootJar -x test --no-daemon

#############################
# Runtime stage
#############################
FROM eclipse-temurin:17-jre

# Create non-root user
RUN addgroup --system app && adduser --system --ingroup app app

WORKDIR /app

# Copy jar from build stage
COPY --from=build /workspace/build/libs/*.jar app.jar

USER app

EXPOSE 8080

# Healthcheck for Docker / orchestration
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- --spider http://localhost:8080/actuator/health || exit 1

# Allow overriding Java options at runtime
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

