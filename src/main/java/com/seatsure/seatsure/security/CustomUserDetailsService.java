package com.seatsure.seatsure.security;

import com.seatsure.seatsure.entity.User;
import com.seatsure.seatsure.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

// Spring Security doesn't know about our User entity out of the box - this
// class is the standard "adapter" interface it calls to look up a user by
// username (we use email as the username) and get back something it
// understands: a UserDetails object.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        // org.springframework.security.core.userdetails.User is Spring
        // Security's OWN built-in UserDetails implementation - unrelated to
        // our entity of the same name. We build one from our entity's data.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // already BCrypt-hashed
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}