package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseUpdatableEntity {

    // 메시지 내용
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 메시지를 작성한 User의 id
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    // 메시지가 작성된 Channel의 id
    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    // 메시지에 첨부된 BinaryContent들의 id 목록
    @ElementCollection
    @CollectionTable(
            name = "message_attachments",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @Column(name = "attachment_id", nullable = false)
    private List<UUID> attachmentIds = new ArrayList<>();

    // 생성자: 메시지 내용, 작성자 id, 채널 id를 받아 Message 객체 생성
    public Message(String content, UUID authorId, UUID channelId) {
        validate(content, authorId, channelId);

        this.content = content;
        this.authorId = authorId;
        this.channelId = channelId;
        this.attachmentIds = new ArrayList<>();
    }

    // 메시지 내용을 수정하는 메서드
    public void update(String content) {
        validateContent(content);

        this.content = content;
        markUpdated();
    }

    // 메시지에 첨부파일 id를 추가하는 메서드
    public void addAttachment(UUID binaryContentId) {
        if (binaryContentId == null) {
            throw new IllegalArgumentException("첨부파일 ID는 비어 있을 수 없습니다.");
        }

        this.attachmentIds.add(binaryContentId);
        markUpdated();
    }

    // 메시지 생성 시 필요한 값들을 검증하는 메서드
    private void validate(String content, UUID authorId, UUID channelId) {
        validateContent(content);

        if (authorId == null) {
            throw new IllegalArgumentException("작성자 ID는 비어 있을 수 없습니다.");
        }

        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 비어 있을 수 없습니다.");
        }
    }

    // 메시지 내용이 null, 빈 문자열, 공백 문자열인지 검증하는 메서드
    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
    }
}