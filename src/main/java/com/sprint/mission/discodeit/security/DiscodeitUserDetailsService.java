package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscodeitUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "사용자를 찾을 수 없습니다. username=" + username
            );
        }

        // 기존 UserService를 이용해 UserDto(UserResponse) 생성
        UserResponse userDto = userService.read(user.getId());

        // Spring Security가 인증에 사용할 UserDetails 반환
        return new DiscodeitUserDetails(
                userDto,
                user.getPassword()
        );
    }
}