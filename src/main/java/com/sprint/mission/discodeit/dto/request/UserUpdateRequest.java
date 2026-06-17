package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserUpdateRequest {

    private UUID userId;

    private String username;
    private String email;
    private String password;

    // 선택적 프로필 이미지 교체
    private String fileName;
    private String contentType;
    private byte[] data;
}