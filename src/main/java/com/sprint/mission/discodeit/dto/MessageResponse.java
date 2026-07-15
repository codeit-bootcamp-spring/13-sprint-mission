package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// 메시지 응답 DTO
@Getter
public class MessageResponse {

    private UUID id;

    private Instant createdAt;

    private Instant updatedAt;

    private String content;

    private UUID authorId;

    private UserResponse author;

    private UUID channelId;

    @JsonIgnore
    private List<UUID> attachmentIds;

    /*
     * 프론트가 실제로 이미지/파일을 렌더링할 때 사용할 첨부파일 정보
     */
    private List<BinaryContentResponse> attachments;

    /*
     * 프론트 호환용 필드
     * 일부 프론트는 message.attachments 대신 message.files를 볼 수 있어서 같이 내려준다.
     */
    private List<BinaryContentResponse> files;

    // 기존 생성자 유지
    public MessageResponse(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            String content,
            UUID authorId,
            UUID channelId,
            List<UUID> attachmentIds
    ) {
        this(
                id,
                createdAt,
                updatedAt,
                content,
                authorId,
                null,
                channelId,
                attachmentIds,
                List.of()
        );
    }

    // 작성자 정보를 포함하는 생성자
    public MessageResponse(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            String content,
            UUID authorId,
            UserResponse author,
            UUID channelId,
            List<UUID> attachmentIds
    ) {
        this(
                id,
                createdAt,
                updatedAt,
                content,
                authorId,
                author,
                channelId,
                attachmentIds,
                List.of()
        );
    }

    // 작성자 + 첨부파일 정보를 포함하는 생성자
    public MessageResponse(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            String content,
            UUID authorId,
            UserResponse author,
            UUID channelId,
            List<UUID> attachmentIds,
            List<BinaryContentResponse> attachments
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.content = content;
        this.authorId = authorId;
        this.author = author;
        this.channelId = channelId;
        this.attachmentIds = attachmentIds;

        this.attachments = attachments == null ? List.of() : attachments;
        this.files = this.attachments;
    }
}