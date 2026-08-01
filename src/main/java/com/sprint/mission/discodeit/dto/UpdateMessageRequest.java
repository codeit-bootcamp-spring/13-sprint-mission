package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(
            max = 1000,
            message = "메시지는 1000자 이하여야 합니다."
    )
    private String content;

    public String getContent() {
        return content;
    }
}