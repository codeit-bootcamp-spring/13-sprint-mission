package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;

import com.sprint.mission.discodeit.security.CertificationChecker;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.RefreshTokenService;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {

    private final String SERVICE_NAME = "AuthService";

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final BinaryContentRepository binaryContentRepository;

    private final CertificationChecker certificationChecker;

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    private final JwtRegistry jwtRegistry;

    public record JwtOutput(
            JwtDto access,
            Cookie refresh
    ){}

    public UserDto roleUpdate(UUID userId, Role role){
        // update query
        User user = userRepository.findById(userId)
                .orElseThrow(RuntimeException::new);

        user.updateRole(role);

        userRepository.save(user);

        // 롤 변경시 기존 로그인 토큰 파기(로그아웃)
        jwtRegistry.invalidateJwtInformationByUserId(user.getId());

        return entityMapper.toDto(user, certificationChecker.online(user.getId()));

    }

    @Override
    public JwtOutput jwtRefresh(String key){

        log.debug("[{}] - 토큰 리프레시 요청 - token : {}", SERVICE_NAME, key);

        RefreshTokenService.Output result = refreshTokenService.rotate(key);

        switch (result.result()) {
            case REVOKED -> throw new RuntimeException();
            case EXPIRED -> throw new RuntimeException();
            case ROTATED -> throw new RuntimeException();
            case NOT_FOUND -> throw new RuntimeException();
            case SUCCESS -> {}
        }


        User user = userRepository.findByUsername(result.username())
                .orElseThrow(UserNotFoundException::new);

        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(),user.getRole());

        UserDto userDto = entityMapper.toDto(
                user,
                certificationChecker.online(user.getId())
        );
        return new JwtOutput(
                new JwtDto(userDto, accessToken),
                refreshCookie(user.getId())
        );
    }


    private Cookie refreshCookie(UUID userId){
        String refreshToken = refreshTokenService.grant(userId);


        // refresh token 쿠키 세팅
        Cookie cookie = new Cookie("REFRESH_TOKEN", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtTokenProvider.refreshTokenExpire());

        return cookie;
    }

}
