package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "messages")
@NoArgsConstructor
public class Message extends BaseUpdatableEntity {

    private UUID id;

    private Instant createdAt;
    private Instant updatedAt;
    private UUID authorId;
    private UUID channelId;
    private String updateContent;

    @Column(columnDefinition = "text")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "message_attachments",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id")
    )
    private List<BinaryContent> attachments = new ArrayList<>();

    public Message(String content) {
        this.id = UUID.randomUUID();
        this.content = content;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.authorId = null;
        this.channelId = null;
        this.updateContent = null;
    }

    public void updateContent(Message requestMessage) {
        this.content = requestMessage.getContent();
        this.updatedAt = Instant.now();
    }


}