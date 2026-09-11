package com.sprint.mission.discodeit.security.userdetail;

import com.sprint.mission.discodeit.dto.response.UserDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
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

    /*
    권한, username(식별자) password 와 같은 유저 정보 반환 매서드.
    todo - email or username 어떤게 로그인에 사용되는지 까먹음. 나중에 수정
     */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userDto.username();
    }


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
