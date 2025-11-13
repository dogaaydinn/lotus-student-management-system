# Multi-stage build for optimized image size
FROM maven:3.8.7-openjdk-8-slim AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM openjdk:8-jre-slim

LABEL maintainer="Lotus Team <support@lotus-spm.com>"
LABEL description="Lotus Student Management System Backend"
LABEL version="2.0.0"

WORKDIR /app

# Create non-root user
RUN groupadd -r lotus && useradd -r -g lotus lotus

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R lotus:lotus /app

# Switch to non-root user
USER lotus

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8085/actuator/health || exit 1

# JVM optimization
ENV JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
