package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserLoginFailedException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.service.basic.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscodeitUserDetailsService implements UserDetailsService {

    private final UserReader userReader;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userReader.getByUsername(username);
            return convertToUserDetails(user);
        } catch (UserLoginFailedException exception) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.", exception);
        }
    }

    private DiscodeitUserDetails convertToUserDetails(User user) {
        return new DiscodeitUserDetails(userMapper.toDto(user, true), user.getPassword());
    }
}
