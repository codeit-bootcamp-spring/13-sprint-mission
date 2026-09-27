package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;

import com.sprint.mission.discodeit.security.SessionService;
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
    private final UserRepository userRepository;
    private final MapStructMapper mapper;
    private final BinaryContentRepository binaryContentRepository;

    private final SessionService sessionService;

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

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

        // find after update
        UserProjection projection = userRepository.getUserFromId(userId)
                .orElseThrow(RuntimeException::new);

        return mapper.toDto(
                projection,
                binaryContentRepository.getBinaryContentById(projection.profileId()).orElse(null),
                sessionService.userOnline(projection.username())
        );

    }

    @Override
    public JwtOutput jwtRefresh(String key){

        RefreshTokenService.Output result = refreshTokenService.rotate(key);

        switch (result.result()) {
            case REVOKED -> new RuntimeException();
            case EXPIRED -> new RuntimeException();
            case ROTATED -> new RuntimeException();
            case NOT_FOUND -> new RuntimeException();
            case SUCCESS -> {}
        }


//        User user = userRepository.findById()

        User user = userRepository.findByUsernameOptional(result.username())
                .orElseThrow(UserNotFoundException::new);

        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(),user.getRole());

        UserDto userDto = mapper.toDto(
                user,
                getProfileToDto(user.getProfile()),
                sessionService.userOnline(user.getUsername())
        );
        return new JwtOutput(
                new JwtDto(userDto, accessToken),
                refreshCookie(user.getId())
        );
    }

    private BinaryContentDto getProfileToDto(BinaryContent profile){
        if (profile == null) return null;
        return binaryContentRepository.getBinaryContentById(profile.getId()).orElse(null);
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
