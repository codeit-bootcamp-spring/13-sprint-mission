package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;

// 데이터 전송 DTO
public record UserCreateRequest(
        String username,
        String email,
        String password,
        BinaryContentCreateRequest profileImage
) { }

