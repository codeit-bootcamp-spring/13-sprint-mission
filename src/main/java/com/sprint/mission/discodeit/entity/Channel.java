package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel extends AllApply {

	String name;
	String nameDescription;
	User creator;
	channelType type;
	
	
	public Channel(String name, String nameDescription, User creator, channelType type) {
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
	
	public channelType getType() {
		return type;
	}
	
	public void update(String name, String nameDescription) {
		this.name = name;
		this.nameDescription = nameDescription;
		updateTime();
	}
	
	public enum channelType{
		TEXT,
		VOICE,
		DIRECT_MESSAGE;
		
	}
	
}
