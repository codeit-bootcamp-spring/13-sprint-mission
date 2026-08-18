package com.sprint.mission.discodeit.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Create 의 경우 새 정보를 입력해야 하는 상황.
 * 아래의 3가지 정보는 필수
 */

public record UserCreateRequest (
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password
){}
