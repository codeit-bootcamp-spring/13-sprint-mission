package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 메시지 수정 요청 DTO
@Getter
@NoArgsConstructor
public class MessageUpdateRequest {

    // 수정할 메시지의 id
    private UUID id;

    // 기존 프론트/기존 코드 호환용
    @Size(
            max = 1000,
            message = "메시지 내용은 1000자 이하여야 합니다."
    )
    private String content;

    // API 명세 v1.2 기준 필드명
    @Size(
            max = 1000,
            message = "새 메시지 내용은 1000자 이하여야 합니다."
    )
    private String newContent;

    public MessageUpdateRequest(UUID id, String content) {
        this.id = id;
        this.content = content;
        this.newContent = content;
    }

    /*
     * 서비스 코드에서는 getContent()를 그대로 사용해도 됨.
     * 요청에 newContent가 들어오면 newContent를 우선 사용하고,
     * 없으면 기존 content를 사용함.
     */
    public String getContent() {
        if (newContent != null && !newContent.isBlank()) {
            return newContent;
        }

        return content;
    }

    @AssertTrue(message = "수정할 메시지 내용은 필수입니다.")
    public boolean isContentProvided() {
        return (newContent != null && !newContent.isBlank())
                || (content != null && !content.isBlank());
    }
}