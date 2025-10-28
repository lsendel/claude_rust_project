# T092: Multi-Stage Dockerfile for Spring Boot Backend
# Multi-Tenant SaaS Platform - Optimized Production Build

##############################################################################
# Stage 1: Build Stage
##############################################################################
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy dependency definitions first (for layer caching)
COPY pom.xml ./
COPY src ./src

# Build the application (skip tests in Docker build - run in CI)
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Extract layered JAR for optimized Docker layers
RUN mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

##############################################################################
# Stage 2: Runtime Stage
##############################################################################
FROM eclipse-temurin:21-jre-jammy

# Add labels for image metadata
LABEL maintainer="SaaS Platform Team" \
      version="1.0.0" \
      description="Multi-Tenant SaaS Platform Backend API"

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Install dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy application layers from build stage (optimized for caching)
COPY --from=build /app/target/dependency/BOOT-INF/lib ./lib
COPY --from=build /app/target/dependency/META-INF ./META-INF
COPY --from=build /app/target/dependency/BOOT-INF/classes ./

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check endpoint (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization flags for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom"

# Enable X-Ray tracing
ENV AWS_XRAY_TRACING_NAME="backend-api"

# Start application using Spring Boot layered JAR approach
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp .:./lib/* com.platform.saas.SaasPlatformApplication"]

# Alternative entrypoint with debugging enabled (comment out above, uncomment below)
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -cp .:./lib/* com.platform.saas.SaasPlatformApplication"]
