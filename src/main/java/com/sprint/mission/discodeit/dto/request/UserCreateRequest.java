package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCreateRequest {

    private String username;
    private String email;
    private String password;

    // 선택적 프로필 이미지
    private String fileName;
    private String contentType;
    private byte[] data;
}