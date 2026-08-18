package com.sprint.mission.discodeit.dto.request.user;

import jakarta.validation.constraints.Email;

/**
 * 클라이언트 측 에서 변경값이 없을 경우, 반환값이 없음.
 * -> null 값일 가능성 존재.
 */

public record UserUpdateRequest(
        String newUsername,
        @Email String newEmail,
        String newPassword
) {}
