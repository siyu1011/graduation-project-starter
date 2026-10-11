package com.example.starter.auth;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class SessionUser implements UserDetails {
    private final Long id;
    private final String username;
    private final String displayName;
    private final String role;
    private final String passwordHash;
    private final boolean enabled;
    public SessionUser(Long id, String username, String displayName, String role, String passwordHash, boolean enabled) {
        this.id = id; this.username = username; this.displayName = displayName;
        this.role = role; this.passwordHash = passwordHash; this.enabled = enabled;
    }
    public Long id() { return id; }
    public CurrentUser view() { return new CurrentUser(id.toString(), username, displayName, role); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_" + role)); }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isEnabled() { return enabled; }
}
