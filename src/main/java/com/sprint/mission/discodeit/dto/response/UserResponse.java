package com.sprint.mission.discodeit.dto.response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponse {

    // 비밀번호 제외, 온라인 상태 포함
    private UUID id;
    private String username;
    private String email;
    private boolean online;

    public UserResponse(
            UUID id,
            String username,
            String email,
            boolean online
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.online = online;
    }
}