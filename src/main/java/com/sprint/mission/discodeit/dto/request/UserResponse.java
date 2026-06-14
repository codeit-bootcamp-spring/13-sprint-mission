package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        Boolean isOnline
) {
}
/*
패스워드 정보 제외, id 추가
사용자의 온라인 상태, username, email
 */