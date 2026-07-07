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

    @ManyToOne(cascade = CascadeType.REMOVE,optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User author;

   @ManyToMany(fetch = FetchType.LAZY)
   @JoinTable(
           name = "message_attachments"
           , joinColumns = @JoinColumn(name = "message_id")
           , inverseJoinColumns = @JoinColumn(name = "attachment_id")
   )
   private List<BinaryContent> attachment;

   public  Message(
           String content,
           Channel channel,
           User user,
           List<BinaryContent> attachment
   ){
       this.content = content;
       this.channel = channel;
       this.author = user;
       this.attachment = attachment;
   }

}
