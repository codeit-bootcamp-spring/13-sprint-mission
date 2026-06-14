package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter

public class Message extends MutableEntity {

	private List<UUID> attachmentIds;

	String content;
	User author;
	Channel channel;
	
	public Message(String content,User author,Channel channel, List<UUID> attachmentIds) {
		super();
		this.content = content;
		this.author = author;
		this.channel = channel;
		this.attachmentIds = attachmentIds;
	}
	
	public void author(User author) {
		this.author = author;
	}
	public void update(String content) {
		this.content = content;

		updateTime();
	}
	
	@Override
	public String toString() {
		return "[Channel: " + getChannel().getName() + "] " + "\n" +
				"Author: " + getAuthor().getName()+ "\n" +
				"Content: " + getContent() + "\n" +
				"Time: " + getUpdateAt()+ "\n";
	}
	
}


