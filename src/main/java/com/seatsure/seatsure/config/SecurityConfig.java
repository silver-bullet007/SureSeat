package com.seatsure.seatsure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    // A @Bean method: we're telling Spring "here's how to construct this
    // object; manage it as a singleton and hand it out wherever it's
    // @Autowired/constructor-injected." This is how you plug a specific
    // implementation into Spring's dependency injection system for a type
    // (PasswordEncoder) that has multiple possible implementations.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}