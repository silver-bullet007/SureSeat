package com.seatsure.seatsure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter guarantees this filter's logic runs exactly one time
// per incoming request, regardless of internal servlet forwarding/dispatch -
// the standard base class for this kind of "runs on every request" filter.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No token, or not the expected "Bearer <token>" format - let the
        // request continue unauthenticated. Spring Security's own rules
        // (configured next) decide whether that's actually allowed for
        // this specific endpoint.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // strip "Bearer " prefix

        try {
            String email = jwtUtil.extractEmail(token);

            // Only proceed if we extracted an email AND there's no existing
            // authentication already set for this request.
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!jwtUtil.isTokenExpired(token)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // THIS is the actual moment Spring Security considers
                    // this request "authenticated" - everything downstream
                    // (controllers, @PreAuthorize checks) can now see who
                    // the current user is via SecurityContextHolder.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Invalid/malformed/tampered token - we deliberately swallow the
            // exception here and just leave the request unauthenticated,
            // rather than crashing the filter chain. Spring Security's own
            // access rules will then correctly reject it as unauthorized.
            logger.warn("Invalid JWT token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}