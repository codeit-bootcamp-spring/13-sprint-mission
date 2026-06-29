package com.sprint.mission.discodeit.dto.message;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record MessageCreateRequest(
        @Schema(description = "채널 ID", example = "383b89f5-3200-4504-9283-2d46cd007444", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID channelId,
        @Schema(description = "작성자 ID", example = "0df2cecb-ff53-49c0-a522-a6065eeac3b9", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID authorId,
        @Schema(description = "메시지", example = "안녕하세요", requiredMode = Schema.RequiredMode.REQUIRED)
        String content

)
{
    public MessageCreateRequest {
        validate(channelId, "채널 id");
        validate(authorId, "작성자 id");
    }

    private void validate(UUID validate, String fieldName){
        if(validate == null){
            throw new IllegalArgumentException(fieldName + "는 필수 값 입니다.");
        }
    }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을 입력해주세요.");
        }
    }
}
