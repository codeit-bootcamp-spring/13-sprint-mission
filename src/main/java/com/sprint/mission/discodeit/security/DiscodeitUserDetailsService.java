package com.sprint.mission.discodeit.security;


import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscodeitUserDetailsService implements UserDetailsService {

    private final String SERVICE_NAME = "DiscodeitUserDetailsService";

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    private final CertificationChecker certificationChecker;

    /*
    UserPassword...filter 가 폼 데이터에서 UserPassword...Token 생성.
    UserPassword...Token 에서 DaoAuthenticationProvider 가 해당 매서드를 호출해서
    Authentication 의 principal 에 로드한다.
     */

    /**
     * username(폼 로그인 정보 == user.email) 을 기준으로 유저 정보 체크.
     * @param username the username identifying the user whose data is required.
     * @return UserDetail 커스텀 객체.
     * @throws UsernameNotFoundException
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // user id key set
        UUID userId;
        if (userRepository.existsByEmail(username))
            userId = userRepository
                    .findIdFromEmail(username)
                    .orElseThrow(() -> new UserNotFoundException(
                            "{} - 인증 유저 조회 에러 - username = {}",
                            SERVICE_NAME,
                            username)
                    );
        else if (userRepository.existsByUsername(username))
            userId = userRepository
                    .findIdFromUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(
                            "{} - 인증 유저 조회 에러 - username = {}",
                            SERVICE_NAME,
                            username)
                    );
        else
            userId = UUID.fromString(username);

        // set UserDetail
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                            "{} - 인증 유저 조회 에러",
                            SERVICE_NAME,
                            userId
                    )
                );

        UserDto dto = entityMapper.toDto(user, certificationChecker.online(userId));

        return new DiscodeitUserDetails(dto, user.getPassword());
    }
}
