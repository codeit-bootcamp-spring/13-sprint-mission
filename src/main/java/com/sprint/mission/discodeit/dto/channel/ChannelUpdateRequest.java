package com.sprint.mission.discodeit.dto.channel;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChannelUpdateRequest(
        @Schema(description = "변경 할 채널 이름", example = "소개 채널", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "변경 할 채널 설명", example = "소개 채널 입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String description

)
{
    public ChannelUpdateRequest {
        validate(name, "채널명");
        validate(description, "채널설명");
    }

    private static void validate(String value, String fieldName) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을 입력해주세요.");
        }
    }
}
