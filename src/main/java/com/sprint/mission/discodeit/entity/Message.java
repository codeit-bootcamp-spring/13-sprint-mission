package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
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

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @OneToMany
    @JoinTable(
            name = "message_attachments",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id")
    )
    private List<BinaryContent> attachments = new ArrayList<>();

    public Message(String content, User author, Channel channel) {
        validate(content, author, channel);

        this.content = content;
        this.author = author;
        this.channel = channel;
        this.attachments = new ArrayList<>();
    }

    public void update(String content) {
        validateContent(content);

        this.content = content;
        markUpdated();
    }

    public void addAttachment(BinaryContent binaryContent) {
        if (binaryContent == null) {
            throw new IllegalArgumentException("첨부파일은 비어 있을 수 없습니다.");
        }

        this.attachments.add(binaryContent);
        markUpdated();
    }

    public UUID getAuthorId() {
        return author == null ? null : author.getId();
    }

    public UUID getChannelId() {
        return channel == null ? null : channel.getId();
    }

    public List<UUID> getAttachmentIds() {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

        return attachments.stream()
                .map(BinaryContent::getId)
                .toList();
    }

    private void validate(String content, User author, Channel channel) {
        validateContent(content);

        if (author == null) {
            throw new IllegalArgumentException("작성자는 비어 있을 수 없습니다.");
        }

        if (channel == null) {
            throw new IllegalArgumentException("채널은 비어 있을 수 없습니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
    }
}