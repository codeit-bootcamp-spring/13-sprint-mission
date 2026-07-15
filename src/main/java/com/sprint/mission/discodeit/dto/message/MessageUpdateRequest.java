package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.command.message.MessageUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;

public record MessageUpdateRequest(
        @Schema(description = "수정 메시지", example = "수정 메시지 입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
)
{
    public MessageUpdateRequest {
        validate(content, "메시지");
    }


    private static void  validate(String value, String fieldName){
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을 입력해주세요.");
        }
    }

    public MessageUpdateCommand toCommand(){
        return new MessageUpdateCommand(content);
    }
}
