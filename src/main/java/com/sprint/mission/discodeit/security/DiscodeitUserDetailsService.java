package com.sprint.mission.discodeit.security;


import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionRegistry;
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

    private final UserRepository userRepository;
    private final MapStructMapper mapper;
    private final BinaryContentRepository binaryContentRepository;

    private final SessionRegistry sessionRegistry;

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
                    .orElseThrow(UserNotFoundException::new);
        else
            userId = UUID.fromString(username);

        // set UserDetail
        UserProjection projection = userRepository.getUserFromId(userId)
                .orElseThrow(() -> {
                    log.warn("UserDetails - 유저 조회 오류 : {}",username);
                    return new DiscodeitException(ExceptionCode.AUTH_FAILURE,"인증 오류");
                });

        UserDto dto = mapper.toDto(
                projection,
                binaryContentRepository.getBinaryContentById(projection.profileId()).orElse(null),
                true    // todo - 온라인 로직 고려해서 값 변경 필요 or 온라인 정보 필요없으면 별도 유저 정보 객체로 사용.
        );

        return new DiscodeitUserDetails(dto, projection.password());
    }
}
