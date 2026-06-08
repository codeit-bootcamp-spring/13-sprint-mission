package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(
        UUID channelId,
        UUID authorId,
        String content,
        List<BinaryContentCreateRequest> attachments

)
{
    public MessageCreateRequest {
        validate(content, "메시지");
    }

    private static void validate(String content, String fieldName) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을 입력해주세요.");
        }
    }
}
