# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Install Playwright browsers in a known location
ENV PLAYWRIGHT_BROWSERS_PATH=/playwright-browsers

# ස්ථාවර Ubuntu Repositories පාවිච්චි කිරීම (Fix for 404 errors)
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates && \
    sed -i 's|http://.*archive.ubuntu.com/ubuntu/|http://mirror.kakao.com/ubuntu/|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com/ubuntu|http://mirror.kakao.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update -y && \
    apt-get install -y --no-install-recommends \
    libgbm1 libnss3 libnspr4 libasound2t64 libatk1.0-0 libatk-bridge2.0-0 \
    libcups2 libdrm2 libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 \
    libpango-1.0-0 libcairo2 libxfixes3 libxshmfence1 fonts-liberation \
    && rm -rf /var/lib/apt/lists/*

# Install Chromium using Playwright CLI
RUN mvn exec:java -e \
    -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.args="install --with-deps chromium"

# Production stage
# Ubuntu Jammy පදනම් කරගත් ස්ථාවර image එකක් පාවිච්චි කිරීම
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Production Stage එකේදී අවශ්‍ය libraries ස්ථාපනය
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl && \
    sed -i 's|http://.*archive.ubuntu.com/ubuntu/|http://mirror.kakao.com/ubuntu/|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com/ubuntu|http://mirror.kakao.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update -y && \
    apt-get install -y --no-install-recommends \
    libgbm1 libnss3 libnspr4 libasound2t64 libatk1.0-0 libatk-bridge2.0-0 \
    libcups2 libdrm2 libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 \
    libpango-1.0-0 libcairo2 libxfixes3 libxshmfence1 libglu1-mesa \
    fonts-liberation fonts-noto-color-emoji libx11-6 libxext6 libxrender1 \
    && rm -rf /var/lib/apt/lists/*

# Create spring user and group
RUN addgroup --system spring && \
    adduser --system --group --home /home/spring spring

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Copy Playwright browsers
COPY --from=build --chown=spring:spring /playwright-browsers /home/spring/.cache/ms-playwright

# Set proper ownership
RUN chown -R spring:spring /app /home/spring

# Switch to spring user
USER spring
WORKDIR /app

# Set environment variables
ENV PLAYWRIGHT_BROWSERS_PATH=/home/spring/.cache/ms-playwright \
    HOME=/home/spring \
    PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

EXPOSE 6060

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:6060/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
