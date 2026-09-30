package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
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

    private final UserDto userDto;
    private final String password;

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 접두어 "ROLE_" 은 관례적으로 붙임.
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + userDto.role())
        );
    }

    /*
    DaoAuthenticationProvider 에서 비밀번호를 검증할 때
    Encoding 된 비밀번호 정보를 반환할 때 쓰이는 매서드.
     */
    @Override
    public String getPassword() {
        return password;
    }

    // 유저 pk(id) 를 반환.
    @Override
    public String getUsername() { return userDto.id().toString(); }


    /*
    세션 동시 로그인 시의 동일 세션 검증 위한 동일 객체 검사 로직.
     */

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (!(o instanceof DiscodeitUserDetails instance)) return false;
        return Objects.equals(userDto.id(), instance.userDto.id()) && Objects.equals(password, instance.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userDto.id(), password);
    }
}
