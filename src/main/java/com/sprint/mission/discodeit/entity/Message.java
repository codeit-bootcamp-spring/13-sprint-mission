package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter

public class Message extends MutableEntity {

	private List<UUID> messageId;

	String content;
	User author;
	Channel channel;
	
	public Message(String content,User author,Channel channel) {
		super();
		this.content = content;
		this.author = author;
		this.channel = channel;
	}
	
	public void author(User author) {
		this.author = author;
	}
	public void update(String content) {
		// [생각해볼 점] 밖에서 channel.getType()을 확인한 후 이 함수를 호출하게 됩니다.
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


