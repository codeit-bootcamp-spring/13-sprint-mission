package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@Getter
@ToString
@Entity
@Table(name = "messages")
public class Message extends BaseUpdatableEntity{

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = true)
    private User author;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "message_attachments",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id")
    )
    private List<BinaryContent> attachments = new ArrayList<>();


    protected Message() {}


    // 텍스트 + 첨부파일

    public Message(String content, Channel channel, User author, List<BinaryContent> attachments) {
        super();
        this.content = content;
        this.channel = channel;
        this.author = author;
        this.attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
