package com.transport.erp.auth.security;

import com.transport.erp.auth.domain.User;
import com.transport.erp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

                return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .disabled(!user.isActive())
                                .authorities(getAuthorities(user))
                                .build();
        }

        private Collection<? extends GrantedAuthority> getAuthorities(User user) {
                // ROLE_ prefixed authority for the role itself (used by hasRole())
                SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

                // Fine-grained permissions derived from the role enum (used by hasAuthority())
                Set<SimpleGrantedAuthority> permissionAuthorities = user.getRole().permissions().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toSet());

                return Stream.concat(Stream.of(roleAuthority), permissionAuthorities.stream())
                                .collect(Collectors.toSet());
        }
}
