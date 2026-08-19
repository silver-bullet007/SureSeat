package com.seatsure.seatsure.config;

import com.seatsure.seatsure.security.JwtAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Boot auto-registers any bean implementing Filter into the raw
    // servlet container's chain, on top of whatever we wire into Spring
    // Security ourselves via addFilterBefore() below. Without this, JwtAuthFilter
    // would run TWICE per request. This bean explicitly disables that
    // automatic registration, leaving our addFilterBefore() call as the
    // one and only place it's wired in.
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection defends against a browser/session-cookie attack
                // vector that doesn't apply here - we're a stateless, token-based
                // API, so we disable it explicitly rather than fight it.
                .csrf(AbstractHttpConfigurer::disable)

                // Tell Spring Security to NEVER create or use an HTTP session.
                // Every request must carry its own proof of identity (the JWT) -
                // this is what "stateless" actually means in code.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public - anyone can register/login without a token
                        .requestMatchers("/api/auth/**").permitAll()
                        // Public read access to browsing events/seats - no login
                        // needed just to look around
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        // Everything else requires a valid, authenticated request
                        .anyRequest().authenticated())

                // Insert our custom filter BEFORE Spring's own default username/
                // password filter, so JWT-based auth runs first in the chain.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}