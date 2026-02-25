package com.vyxentra.vehicle.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                    .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                    .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                    .authorizeExchange(exchanges -> exchanges
                            .pathMatchers("/api/v1/auth/**").permitAll()
                            .pathMatchers("/api/v1/public/**").permitAll()
                            .pathMatchers("/actuator/health/**").permitAll()
                            .pathMatchers("/actuator/info").permitAll()
                            .pathMatchers("/fallback/**").permitAll()
                            .pathMatchers("/webjars/**").permitAll()
                            .pathMatchers("/swagger-ui.html").permitAll()
                            .pathMatchers("/swagger-ui/**").permitAll()
                            .pathMatchers("/v3/api-docs/**").permitAll()
                            .anyExchange().authenticated()
                    )
                    .build();
        }
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList("*")); // Configure appropriately in production
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
            configuration.setAllowedHeaders(Arrays.asList("*"));
            configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Request-ID"));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
}
