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

RUN apt-get update && apt-get install -y --no-install-recommends \

    libgbm1 \

    libnss3 \

    libnspr4 \

    libasound2t64 \

    libatk1.0-0t64 \

    libatk-bridge2.0-0t64 \

    libcups2t64 \

    libdrm2 \

    libxkbcommon0 \

    libxcomposite1 \

    libxdamage1 \

    libxrandr2 \

    libpango-1.0-0 \

    libcairo2 \

    libxfixes3 \

    libgbm1 \

    fonts-liberation \

    libxshmfence1 \

    && rm -rf /var/lib/apt/lists/*

# Install Chromium using Playwright CLI

RUN mvn exec:java -e \

    -Dexec.mainClass=com.microsoft.playwright.CLI \

    -Dexec.args="install --with-deps chromium"

# Production stage

FROM eclipse-temurin:17-jre

WORKDIR /app

# Install ALL required Chromium dependencies

RUN apt-get update && apt-get install -y --no-install-recommends \

    libgbm1 \

    libnss3 \

    libnspr4 \

    libasound2t64 \

    libatk1.0-0t64 \

    libatk-bridge2.0-0t64 \

    libcups2t64 \

    libdrm2 \

    libxkbcommon0 \

    libxcomposite1 \

    libxdamage1 \

    libxrandr2 \

    libpango-1.0-0 \

    libcairo2 \

    libxfixes3 \

    libxshmfence1 \

    libglu1-mesa \

    fonts-liberation \

    fonts-noto-color-emoji \

    libx11-6 \

    libxext6 \

    libxrender1 \

    ca-certificates \

    curl \

    && rm -rf /var/lib/apt/lists/*

# Create spring user and group

RUN addgroup --system spring && \

    adduser --system --group --home /home/spring spring

# Copy JAR from build stage

COPY --from=build /app/target/*.jar app.jar

# Copy Playwright browsers from build stage to correct location

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

# Health check (optional but recommended)

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \

    CMD curl -f http://localhost:6060/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
