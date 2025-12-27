# Multi-stage build for Spring Boot backend
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. pom.xml එක analyzer ෆෝල්ඩරයෙන් ගන්න (Fix applied here)
COPY analyzer/pom.xml .
RUN mvn dependency:go-offline -B

# 2. src ෆෝල්ඩරයත් analyzer එකෙන් ගන්න (Fix applied here)
COPY analyzer/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage (Mac M1/M2 Compatible)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create system user
RUN addgroup --system spring && adduser --system --group spring

# Copy JAR from build stage
COPY --from=build /app/target/analyzer-*.jar app.jar

# Permissions
RUN chown -R spring:spring /app
USER spring:spring

EXPOSE 6060

# Simple Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD java -version || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
