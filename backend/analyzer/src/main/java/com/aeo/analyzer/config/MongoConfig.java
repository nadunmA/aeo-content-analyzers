// FILE: src/main/java/com/aeo/analyzer/config/MongoConfig.java
// 🔄 REPLACE YOUR CURRENT MongoConfig.java WITH THIS

package com.aeo.analyzer.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:aeo_content_analyzer}")
    private String databaseName;

    @Bean
    public MongoClient mongoClient() {
        try {
            log.info("Initializing MongoDB connection...");

            ConnectionString connectionString = new ConnectionString(mongoUri);

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToSslSettings(builder -> {
                        builder.enabled(true);
                        builder.invalidHostNameAllowed(false);
                    })
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(20, TimeUnit.SECONDS)
                                    .readTimeout(20, TimeUnit.SECONDS)
                    )
                    .applyToClusterSettings(builder ->
                            builder.serverSelectionTimeout(30, TimeUnit.SECONDS)
                    )
                    .retryWrites(true)
                    .retryReads(true)
                    .build();

            MongoClient client = MongoClients.create(settings);

            log.info("✅ MongoDB client created successfully");
            return client;

        } catch (Exception e) {
            log.error("❌ MongoDB client creation failed: {}", e.getMessage(), e);
            throw new RuntimeException(
                    "Failed to connect to MongoDB: " + e.getMessage(),
                    e
            );
        }
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        log.info("Creating MongoTemplate for database: {}", databaseName);
        return new MongoTemplate(mongoClient, databaseName);
    }
}