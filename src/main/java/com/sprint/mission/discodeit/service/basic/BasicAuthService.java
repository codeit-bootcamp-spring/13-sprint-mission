package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtRefreshResult;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.jwt.InvalidJwtException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.*;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDto updateRole(UserRoleUpdateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        user.updateRole(request.newRole());

        // 권한 변경 후 기존 JWT를 전부 무효화
        jwtRegistry.invalidateJwtInformationByUserId(user.getId());

        return userMapper.toDto(user, false);
    }

    @Override
    public JwtRefreshResult refresh(String refreshToken) {
        // refresh 토큰 검증
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new InvalidJwtException();
        }

        // Registry에 현재 활성화된 Refresh Token인지 검증
        if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
            throw new InvalidJwtException();
        }

        String username = jwtTokenProvider.getUsername(refreshToken);

        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) discodeitUserDetailsService.loadUserByUsername(username);
        Role role = userDetails.getUserDto().role();

        // 새 Access, Refresh 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(username, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username, role);

        JwtInformation newJwtInformation = new JwtInformation(
                userDetails.getUserDto(),
                newAccessToken,
                newRefreshToken
        );

        // 기존 Refresh Token의 JwtInformation을 새 정보로 교체 (리프레시 토큰 Rotation)
        jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

        JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), newAccessToken);

        return new JwtRefreshResult(jwtDto, newRefreshToken);
    }

}
