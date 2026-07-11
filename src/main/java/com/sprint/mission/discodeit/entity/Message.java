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

	@Column(name = "content", columnDefinition = "text")
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id")
	private User author;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "channel_id", nullable = false)
	private Channel channel;

	@ManyToMany
	@JoinTable(
			name = "message_attachments",
			joinColumns = @JoinColumn(name = "message_id"),
			inverseJoinColumns = @JoinColumn(name = "attachment_id")
	)
	private List<BinaryContent> attachments = new ArrayList<>();
	
	public Message(String content,User author,Channel channel, List<UUID> attachments) {
		super();
		this.content = content;
		this.author = author;
		this.channel = channel;
		if (attachments != null) {
			this.attachments = getAttachments();
		}
	}

	public void author(User author) {
		this.author = author;
	}

	public void update(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "[Channel: " + getChannel().getName() + "] " + "\n" +
				"Author: " + getAuthor().getName()+ "\n" +
				"Content: " + getContent() + "\n" +
				"Time: " + getUpdateAt()+ "\n";
	}
	
}


