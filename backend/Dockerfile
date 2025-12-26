# Multi-stage build for Spring Boot backend
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# --------------------------------------------------------
# Production stage (Fixed for Mac M1/M2)
# --------------------------------------------------------
# 1. Changed from 'alpine' to standard 'jre' to fix platform error
FROM eclipse-temurin:17-jre
WORKDIR /app

# 2. Changed user creation commands to match non-alpine Linux
RUN addgroup --system spring && adduser --system --group spring

# Copy JAR from build stage (Kept your wildcard logic)
COPY --from=build /app/target/analyzer-*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app
USER spring:spring

# Expose port
EXPOSE 6060

# 3. Simplified Health check (Removed wget dependency)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD java -version || exit 1

# Run application
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]