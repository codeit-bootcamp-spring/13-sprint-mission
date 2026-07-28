package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.command.message.MessageUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MessageUpdateRequest(
        @Schema(description = "수정 메시지", example = "수정 메시지 입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "메시지를 입력해주세요.")
        String content
)
{
    public MessageUpdateCommand toCommand(){
        return new MessageUpdateCommand(content);
    }
}
