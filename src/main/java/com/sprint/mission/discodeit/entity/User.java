package com.sprint.mission.discodeit.entity;


import lombok.ToString;

public class User extends AllApply {
	
	String name;
	String email;
	
	public User(String name, String email) {
		super();
		this.name = name;
		this.email = email;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void renew(String name, String email) {
		this.name = name;
		this.email = email;
		updateTime();
	}

	@Override
	public String toString() {
		return "User[" +
				"id= " + getId() +
				"Name=" + getName() + "\n" +
				"Email=" + getEmail() + "\n" +
				"CreateAt=" + getCreateAt() + "\n" +
				"UpdateAt=" + getUpdateAt() +
				"]";
	}
}
