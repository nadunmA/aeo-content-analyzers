# =========================
# Stage 1: Build stage
# =========================
# Ubuntu Jammy පදනම් කරගත් ස්ථාවර Maven image එකක් පාවිච්චි කිරීම
FROM maven:3.9.9-eclipse-temurin-17-jammy AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Install Playwright browser dependencies
ENV PLAYWRIGHT_BROWSERS_PATH=/playwright-browsers

# ✅ Ubuntu Repositories ස්ථිරවම නිවැරදි කිරීම (Mirror Fix)
RUN sed -i 's|http://.*archive.ubuntu.com/ubuntu/|http://mirror.kakao.com/ubuntu/|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com/ubuntu|http://mirror.kakao.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    libgbm1 \
    libnss3 \
    libnspr4 \
    libasound2 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    libpango-1.0-0 \
    libcairo2 \
    libxfixes3 \
    fonts-liberation \
    libxshmfence1 \
    && rm -rf /var/lib/apt/lists/*

# Install Chromium via Playwright
RUN mvn exec:java \
    -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.args="install chromium"


# =========================
# Stage 2: Production stage
# =========================
# වඩාත් ස්ථාවර Ubuntu Jammy JRE පාවිච්චි කිරීම
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install Chromium runtime dependencies
RUN sed -i 's|http://.*archive.ubuntu.com/ubuntu/|http://mirror.kakao.com/ubuntu/|g' /etc/apt/sources.list && \
    sed -i 's|http://security.ubuntu.com/ubuntu|http://mirror.kakao.com/ubuntu|g' /etc/apt/sources.list && \
    apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    libgbm1 \
    libnss3 \
    libnspr4 \
    libasound2 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
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
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN addgroup --system spring && \
    adduser --system --group --home /home/spring spring

# Copy application JAR
COPY --from=build /app/target/*.jar app.jar

# Copy Playwright browsers
COPY --from=build --chown=spring:spring /playwright-browsers /home/spring/.cache/ms-playwright

# Set permissions
RUN chown -R spring:spring /app /home/spring

USER spring

ENV HOME=/home/spring \
    PLAYWRIGHT_BROWSERS_PATH=/home/spring/.cache/ms-playwright \
    PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

EXPOSE 6060

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:6060/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
