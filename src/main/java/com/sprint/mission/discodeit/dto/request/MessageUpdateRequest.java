package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MessageUpdateRequest(

        @NotNull(message = "메시지 ID는 필수입니다.")
        UUID id,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 1000, message = "메시지는 1000자 이하로 입력해주세요.")
        String content
) {
}
