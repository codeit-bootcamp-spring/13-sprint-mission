package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.auth.InvalidRefreshTokenException;
import com.sprint.mission.discodeit.security.CustomUserDetails;
import com.sprint.mission.discodeit.security.CustomUserDetailsService;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final CustomUserDetailsService userDetailsService;

  public JwtInformation refresh(String refreshToken) {

    if (refreshToken == null
        || !jwtTokenProvider.validateRefreshToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {

      throw new InvalidRefreshTokenException();
    }

    String email = jwtTokenProvider.getEmail(refreshToken);

    CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(
        email);

    UserDto userDto = userDetails.getUserDto();

    String newAccessToken = jwtTokenProvider.createAccessToken(userDto);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userDto);

    JwtInformation newJwtInformation = new JwtInformation(userDto, newAccessToken, newRefreshToken);

    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    return newJwtInformation;
  }
}
