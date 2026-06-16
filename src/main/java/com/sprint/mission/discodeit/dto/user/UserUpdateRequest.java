package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import java.util.UUID;

public record UserUpdateRequest( // 수정 대상 객체의 id 파라미터, 수정할 값 파라미터
        UUID userId,
        String newUsername,
        String newEmail,
        String newPassword,
        BinaryContentCreateRequest newProfileImage
) { }
