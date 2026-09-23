package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.data.UserDto;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 디스코드잇의 사용자 정보를 담는 {@link UserDetails} 구현체.
 * <p>
 * 인증 정보(Principal)에 UserDto를 함께 담아, 인증 이후 컨트롤러에서
 * 별도 조회 없이 사용자 정보에 접근할 수 있도록 한다.
 */
@Getter
@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

    private final UserDto userDto;
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userDto.role().name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userDto.username();
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

  /**
   * SessionRegistry의 기본 구현체는 principal을 Map의 key로 사용하므로
   * equals/hashCode를 재정의해야 세션 추적이 정상 동작한다.
   * 권한이나 온라인 상태가 달라져도 동일 사용자로 인식되도록 식별자만 비교한다.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DiscodeitUserDetails other)) {
      return false;
    }
    return Objects.equals(this.userDto.id(), other.userDto.id());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.userDto.id());
  }
}
