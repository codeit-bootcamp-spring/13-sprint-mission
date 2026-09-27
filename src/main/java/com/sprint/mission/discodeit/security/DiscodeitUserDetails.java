package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DiscodeitUserDetails implements UserDetails {

    private final UserResponse user;
    private final String password;

    public DiscodeitUserDetails(
            UserResponse user,
            String password
    ) {
        this.user = user;
        this.password = password;
    }

    public UserResponse getUser() {
        return user;
    }

    public UUID getUserId() {
        return user.id();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.role().name()
                )
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return user.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}