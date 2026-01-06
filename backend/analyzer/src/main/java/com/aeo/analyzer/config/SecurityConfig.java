package com.aeo.analyzer.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain...");

        http
                // CSRF Disable (REST API )
                .csrf(csrf -> csrf.disable())

                // CORS Enable
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/content/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Security Headers (Merged from WebConfig)
                .headers(headers -> headers
                                .xssProtection(xss -> xss
                                        .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                                )
                                .frameOptions(frame -> frame.deny()) // Clickjacking protection
                                .contentSecurityPolicy(csp -> csp
                                        .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; frame-src 'none'")
                                )

                );

        log.info("Security configuration completed");
        return http.build();
    }


    @Value("${app.frontend.url:http://localhost:2000}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:2000",
                frontendUrl
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Retry-After"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
