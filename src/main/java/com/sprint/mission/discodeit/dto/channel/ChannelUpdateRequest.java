package com.sprint.mission.discodeit.dto.channel;

public record ChannelUpdateRequest(
        String name,
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
