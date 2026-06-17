package com.sprint.mission.discodeit.entity;

import lombok.Getter;

@Getter
public class Channel extends MutableEntity {

	String name;
	String nameDescription;
	User creator;
	ChannelType type;

	
	
	public Channel(String name, String nameDescription, User creator, ChannelType type) {
		super();
		this.name = name;
		this.nameDescription = nameDescription;
		this.creator = creator;
		this.type = type;
	}

	public void update(String name, String nameDescription) {
		this.name = name;
		this.nameDescription = nameDescription;
		updateTime();
	}

	@Override
	public String toString() {
		return "Channel[" +
				"id= " + getId() + "\n" +
				"Name=" + getName() + "\n" +
				"NameDescription=" + getNameDescription() + "\n" +
				"Creator=" + (creator == null ? null : getCreator())+ "\n" +
				"Type=" + getType() + "\n" +
				"]";
	}

	
}
