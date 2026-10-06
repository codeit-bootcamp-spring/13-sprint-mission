package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.UserResponse;
import lombok.Getter;

import java.util.Objects;

@Getter
public class JwtInformation {

    private final UserResponse userDto;

    private String accessToken;

    private String refreshToken;

    public JwtInformation(
            UserResponse userDto,
            String accessToken,
            String refreshToken
    ) {

        this.userDto =
                Objects.requireNonNull(
                        userDto,
                        "userDto는 null일 수 없습니다."
                );

        this.accessToken =
                Objects.requireNonNull(
                        accessToken,
                        "accessToken은 null일 수 없습니다."
                );

        this.refreshToken =
                Objects.requireNonNull(
                        refreshToken,
                        "refreshToken은 null일 수 없습니다."
                );
    }

    /**
     * 토큰 재발급 시
     * 기존 Access / Refresh Token을
     * 새로운 토큰으로 교체한다.
     */
    public void rotate(
            String accessToken,
            String refreshToken
    ) {

        this.accessToken =
                Objects.requireNonNull(
                        accessToken,
                        "accessToken은 null일 수 없습니다."
                );

        this.refreshToken =
                Objects.requireNonNull(
                        refreshToken,
                        "refreshToken은 null일 수 없습니다."
                );
    }
}