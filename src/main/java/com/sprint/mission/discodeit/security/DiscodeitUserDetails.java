package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.UserResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

    // 인증 성공 후 Principal에 보관할 사용자 정보
    private final UserResponse userDto;

    // BCrypt로 암호화되어 DB에 저장된 비밀번호
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + userDto.getRole().name()
                )
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userDto.getUsername();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DiscodeitUserDetails that)) {
            return false;
        }

        return Objects.equals(
                userDto.getId(),
                that.userDto.getId()
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                userDto.getId()
        );
    }
}