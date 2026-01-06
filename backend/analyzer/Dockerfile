# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

ENV PLAYWRIGHT_BROWSERS_PATH=/playwright-browsers


RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl && \
    sed -i 's|http://.*archive.ubuntu.com/ubuntu/|http://mirror.kakao.com/ubuntu/|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com/ubuntu|http://mirror.kakao.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update -y && \
    apt-get install -y --no-install-recommends \
    libgbm1 libnss3 libnspr4 libasound2t64 libatk1.0-0t64 libatk-bridge2.0-0t64 \
    libcups2 libdrm2 libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 \
    libpango-1.0-0 libcairo2 libxfixes3 libxshmfence1 fonts-liberation \
    libglu1-mesa xvfb xauth && \
    rm -rf /var/lib/apt/lists/*


RUN mvn exec:java -e \
    -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.args="install chromium"

# Production stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl && \
    sed -i 's|http://.*archive.ubuntu.com/ubuntu/|http://mirror.kakao.com/ubuntu/|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com/ubuntu|http://mirror.kakao.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update -y && \
    apt-get install -y --no-install-recommends \
    libgbm1 libnss3 libnspr4 libasound2 libatk1.0-0 libatk-bridge2.0-0 \
    libcups2 libdrm2 libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 \
    libpango-1.0-0 libcairo2 libxfixes3 libxshmfence1 libglu1-mesa \
    fonts-liberation fonts-noto-color-emoji libx11-6 libxext6 libxrender1 \
    && rm -rf /var/lib/apt/lists/*

RUN addgroup --system spring && \
    adduser --system --group --home /home/spring spring

COPY --from=build /app/target/*.jar app.jar
COPY --from=build --chown=spring:spring /playwright-browsers /home/spring/.cache/ms-playwright
RUN chown -R spring:spring /app /home/spring

USER spring
WORKDIR /app

ENV PLAYWRIGHT_BROWSERS_PATH=/home/spring/.cache/ms-playwright \
    HOME=/home/spring \
    PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

EXPOSE 6060

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:6060/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
