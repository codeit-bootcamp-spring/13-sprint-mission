package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record MessageUpdateRequest(

        @NotNull(message = "messageId는 필수입니다.")
        UUID messageId,

        @NotBlank(message = "content는 비워둘 수 없습니다.")
        String content,

        List<String> attachmentPathList
) {
}
