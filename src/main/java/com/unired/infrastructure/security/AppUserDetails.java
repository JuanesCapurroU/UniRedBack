package com.unired.infrastructure.security;

import java.time.LocalDateTime;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Builder
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String role;
    private final boolean enabled;
    private final LocalDateTime blockedUntil;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return blockedUntil == null || blockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
