package com.vyxentra.vehicle.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.user.name}")
    private String configServerUsername;

    @Value("${spring.security.user.password}")
    private String configServerPassword;

    @Value("${spring.security.user.roles:CONFIG_SERVER}")
    private String[] configServerRoles;

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity httpSecurity)throws Exception{

          httpSecurity.securityMatcher(actuatorBasePath +"/**")
                  .authorizeHttpRequests(authz
                          ->authz.requestMatchers(actuatorBasePath+"/health/**").permitAll()
                          .requestMatchers(actuatorBasePath + "/info/**").permitAll()
                          .requestMatchers(actuatorBasePath + "/**").hasRole("ACTUATOR")
                  ).httpBasic(Customizer.withDefaults()).csrf(csrf->csrf.disable());
               logger.info("Configured actuator security with base path: {}", actuatorBasePath);
               return httpSecurity.build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain configServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/encrypt/**", "/decrypt/**").hasRole("CONFIG_SERVER")
                        .requestMatchers("/**").hasRole("CONFIG_SERVER")
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());

        logger.info("Configured config server security with basic authentication");
        return http.build();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails configServerUser = User.builder()
                .username(configServerUsername)
                .password(passwordEncoder().encode(configServerPassword))
                .roles(configServerRoles)
                .build();

        UserDetails actuatorUser = User.builder()
                .username("actuator")
                .password(passwordEncoder().encode("actuator@123"))
                .roles("ACTUATOR")
                .build();

        logger.info("Created users: {} (roles: {}), actuator (roles: ACTUATOR)",
                configServerUsername, String.join(",", configServerRoles));

        return new InMemoryUserDetailsManager(configServerUser, actuatorUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
