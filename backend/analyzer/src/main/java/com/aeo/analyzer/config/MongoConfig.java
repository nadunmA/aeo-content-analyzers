package com.aeo.analyzer.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.aeo.analyzer.exception.MongoConnectionException;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:aeo_content_analyzer}")
    private String databaseName;

    @Bean
    public MongoClient mongoClient() {
        try {
            // Force TLS 1.2 protocol
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
            System.setProperty("https.protocols", "TLSv1.2");

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

            return MongoClients.create(settings);

        } catch (Exception e) {
            throw new MongoConnectionException(
                    "MongoDB client creation failed: " + e.getMessage(),
                    e
            );
        }
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, databaseName);
    }
}