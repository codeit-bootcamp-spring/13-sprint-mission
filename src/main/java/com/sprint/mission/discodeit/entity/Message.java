package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "messages")
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseUpdatableEntity {

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User author;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "message_attachments",
            joinColumns = @JoinColumn(name = "message_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "attachment_id", nullable = false)
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<BinaryContent> attachments;

    public Message(User author, Channel channel, String content) {
        this(author, channel, content, new ArrayList<>());
    }

    public Message(User author, Channel channel, String content, List<BinaryContent> attachments) {
        super();
        this.author = author;
        this.channel = channel;
        this.content = content;
        this.attachments = attachments;
    }

    public void update(String content) {
        this.content = content;
    }
}
