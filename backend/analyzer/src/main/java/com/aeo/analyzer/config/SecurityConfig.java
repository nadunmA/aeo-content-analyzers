package com.aeo.analyzer.config;

import lombok.extern.slf4j.Slf4j;
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
                // 1. CSRF Disable (REST API නිසා)
                .csrf(csrf -> csrf.disable())

                // 2. CORS Enable
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Stateless Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Authorization Rules (URL Fix is here! 👇)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/content/**").permitAll() // ✅ Added /v1/
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 5. Security Headers (Merged from WebConfig)
                .headers(headers -> headers
                                .xssProtection(xss -> xss
                                        .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                                )
                                .frameOptions(frame -> frame.deny()) // Clickjacking protection
                                .contentSecurityPolicy(csp -> csp
                                        .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; frame-src 'none'")
                                )
                        // Permissions Policy header එක Spring Security වලින් දාන්න අමාරු නිසා
                        // අපි ඒක Nginx වලින් පස්සේ දාමු. දැනට මේ ඇති.
                );

        log.info("✅ Security configuration completed");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS...");

        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed Origins
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:2000"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Retry-After"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // ✅ CORS path එකත් Version එකට ගැලපෙන්න හැදුවා
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}