package com.sprint.mission.discodeit.dto.message;

public record MessageUpdateRequest(
        String content
)
{
    public MessageUpdateRequest {
        validate(content, "메시지");
    }


    private static void  validate(String content, String fieldName){
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(fieldName + "을 입력해주세요.");
        }
    }
}
