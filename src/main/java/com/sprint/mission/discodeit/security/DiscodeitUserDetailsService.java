package com.sprint.mission.discodeit.security;


import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscodeitUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MapStructMapper mapper;
    private final BinaryContentRepository binaryContentRepository;

    private final SessionRegistry sessionRegistry;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // UserDto, password

        UserProjection projection = userRepository.getUserFromUsername(username)
                .orElseThrow(() -> {
                    log.warn("UserDetails - 유저 조회 오류 : {}",username);
                    return new DiscodeitException(ExceptionCode.AUTH_FAILURE,"인증 오류");
                });

        // 본인 정보 반환이라 online true 반환.
        UserDto dto = mapper.toDto(
                projection,
                binaryContentRepository.getBinaryContentById(projection.profileId()).orElse(null),
                true
        );

        return new DiscodeitUserDetails(dto, projection.password());
    }
}
