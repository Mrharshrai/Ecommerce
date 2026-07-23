package com.shop.userservice.user_service.service;

import com.shop.userservice.user_service.entity.User;
import com.shop.userservice.user_service.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true) // Ensures session is open for Lazy loading
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String roleName = role.name();
                    // Avoid double prefixing if the DB already has "ROLE_"
                    return new SimpleGrantedAuthority(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
                })
                .collect(Collectors.toSet());
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(user.isDeleted())
                .build();
    }
}
