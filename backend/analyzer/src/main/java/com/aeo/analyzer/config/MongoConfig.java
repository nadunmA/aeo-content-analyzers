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

    @Value("${spring.data.mongodb.host:localhost}")
    private String host;

    @Value("${spring.data.mongodb.port:27017}")
    private String port;

    @Value("${spring.data.mongodb.database:aeo_analyzer}")
    private String database;

    @Value("${spring.data.mongodb.username:admin}")
    private String username;

    @Value("${spring.data.mongodb.password:admin123}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database:admin}")
    private String authDatabase;

    @Bean
    public MongoClient mongoClient() {
        try {
            log.info("Initializing MongoDB connection...");
            log.info("MongoDB Host: {}:{}", host, port);
            log.info("MongoDB Database: {}", database);

            String connectionString = String.format(
                    "mongodb://%s:%s@%s:%s/%s?authSource=%s",
                    username, password, host, port, database, authDatabase
            );

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .applyToSslSettings(builder -> {
                        builder.enabled(false);  // ✅ SSL disabled
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
        log.info("Creating MongoTemplate for database: {}", database);
        return new MongoTemplate(mongoClient, database);
    }
}