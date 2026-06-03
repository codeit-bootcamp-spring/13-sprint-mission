package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel extends AllApply {

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
	
	public String getName() {
		return name;
	}
	
	public String getNameDescription() {
		return nameDescription;
	}
	
	public User getCreator() {
		return creator;
	}

	public ChannelType getType() {
		return type;
	}


	public void update(String name, String nameDescription) {
		this.name = name;
		this.nameDescription = nameDescription;
		updateTime();
	}

	@Override
	public String toString() {
		return "Channel[" +
				"id= " + getId() +
				"Name=" + getName() + "\n" +
				"NameDescription=" + getNameDescription() + "\n" +
				"Creator=" + (creator == null ? null : getCreator())+ "\n" +
				"Type=" + getType() + "\n" +
				"]";
	}

	
}
