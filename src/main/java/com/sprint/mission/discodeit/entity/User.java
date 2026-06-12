package com.sprint.mission.discodeit.entity;


import lombok.Getter;

import java.util.UUID;

@Getter
public class User extends MutableEntity {

	private UUID profileId;
	
	String name;
	String email;
	String password;
	
	public User(String name, String email, String password, UUID profileId) {
		super();
		this.name = name;
		this.email = email;
		this.password = password;
		this.profileId = profileId;
	}

	public void renew(String name, String email, String password, UUID profileId) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.profileId = profileId;
		updateTime();
	}

	@Override
	public String toString() {
		return "User[" +
				"id= " + getId() + "\n" +
				"Name=" + getName() + "\n" +
				"Email=" + getEmail() + "\n" +
				"CreateAt=" + getCreateAt() + "\n" +
				"UpdateAt=" + getUpdateAt() + "\n" +
				"]";
	}
}
