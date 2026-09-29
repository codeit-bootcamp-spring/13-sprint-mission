package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import lombok.Getter;

import java.util.Objects;

@Getter
public class JwtInformation {

    private final UserDto userDto;
    private volatile String accessToken;
    private  volatile String refreshToken;

    public JwtInformation(UserDto userDto, String accessToken, String refreshToken) {

        this.userDto = Objects.requireNonNull(userDto, "사용자 정보는 필수입니다.");
        this.accessToken = Objects.requireNonNull(accessToken, "Access Token은 필수입니다.");
        this.refreshToken = Objects.requireNonNull(refreshToken," Refresh Token은 필수입니다.");
    }

    public synchronized void rotate(String accessToken, String refreshToken) {
        this.accessToken = Objects.requireNonNull(
                accessToken,
                "Access Token은 필수입니다."
        );
        this.refreshToken = Objects.requireNonNull(
                refreshToken,
                "Refresh Token은 필수입니다."
        );
    }

}
