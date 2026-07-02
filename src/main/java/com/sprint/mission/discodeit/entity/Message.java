package com.sprint.mission.discodeit.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "messages")
public class Message extends BaseUpdatableEntity {
    @Column
    private String content;

    @ManyToOne(cascade = CascadeType.REMOVE,optional = false)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User author;

   @ManyToMany
   @JoinTable(
           name = "message_attachments"
           , joinColumns = @JoinColumn(name = "attachment_id")
           , inverseJoinColumns = @JoinColumn(name = "message_id")
   )
    private List<BinaryContent> attachment;
}
