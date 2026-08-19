package com.seatsure.seatsure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    // Private constructor - this class only holds a static helper method,
    // it's never meant to be instantiated.
    private SecurityUtils() {
    }

    // Pulls the authenticated user's email (their "username", in Spring
    // Security's generic terminology) out of whatever JwtAuthFilter set
    // into SecurityContextHolder earlier in this same request's lifecycle.
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        return authentication.getName(); // returns the UserDetails username - our email
    }
}