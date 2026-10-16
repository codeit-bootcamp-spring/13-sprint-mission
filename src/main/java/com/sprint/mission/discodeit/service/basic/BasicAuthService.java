package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.role.RoleUpdateEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.UserRepository;

import com.sprint.mission.discodeit.security.CertificationChecker;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {

    private final String SERVICE_NAME = "AuthService";

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    private final CertificationChecker certificationChecker;
    private final JwtTokenProvider jwtTokenProvider;

    private final JwtRegistry jwtRegistry;

    private final ApplicationEventPublisher applicationEventPublisher;


    public UserDto roleUpdate(UUID userId, Role role){
        // update query
        User user = userRepository.findById(userId) // 유저가 없음?
                .orElseThrow(RuntimeException::new);

        Role before = user.getRole();
        Role after = role;

        // message event 발행.
        applicationEventPublisher.publishEvent(
                new RoleUpdateEvent(user.getId(), before, after)
        );

        user.updateRole(after);

        userRepository.save(user);

        // 롤 변경시 기존 로그인 토큰 파기(로그아웃)
        jwtRegistry.invalidateJwtInformationByUserId(user.getId());

        return entityMapper.toDto(user, certificationChecker.online(user.getId()));

    }

    @Override
    public JwtInformation jwtRefresh(String refreshToken){
        // 1. 리프레시 토큰 검정 -> 토큰이 만료된건 아닌지, 토큰이 맞게 검정되어있는지.
        // 2. 토큰 둘다 리프레시.
        // 3. 두 토큰 다 반환.

        if (refreshToken == null) throw new DiscodeitException(ExceptionCode.REQUEST_VALUE_ERROR,"빈 토큰이 요청됨.");

        if (
                jwtTokenProvider.isExpired(refreshToken)
                || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)
        ) {
            throw new DiscodeitException(ExceptionCode.EXPIRED_TOKEN,"[{}] - 토큰이 만료됨. {}", SERVICE_NAME, refreshToken);
        }

        log.debug("[{}] - 토큰 리프레시 개시 - token : {}", SERVICE_NAME, refreshToken);

        String userId = jwtTokenProvider.getSubject(refreshToken);

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(UserNotFoundException::new);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId().toString(),user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId().toString());

        UserDto userDto = entityMapper.toDto(
                user,
                certificationChecker.online(user.getId())
        );

        JwtInformation newInfo = new JwtInformation(
                userDto,
                newAccessToken,
                newRefreshToken
        );

        jwtRegistry.rotateJwtInformation(refreshToken, newInfo);

        return newInfo;
    }




}
