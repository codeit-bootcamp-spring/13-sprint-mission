package com.sprint.mission.discodeit.entity;

public class Message extends AllApply {

	String content;
	User author;
	Channel channel;
	
	public Message(String content,User author,Channel channel) {
		super();
		this.content = content;
		this.author = author;
		this.channel = channel;
	}
	
	public String getContent() {
		return content;
	}
	
	public User getAuthor() {
		return author;
	}
	public Channel getChannel() {
		return channel;
	}
	
	public void author(User author) {
		this.author = author;
	}
	public void update(String content) {
		// [생각해볼 점] 밖에서 channel.getType()을 확인한 후 이 함수를 호출하게 됩니다.
		this.content = content;
		updateTime();
	}
	
	
	
}


