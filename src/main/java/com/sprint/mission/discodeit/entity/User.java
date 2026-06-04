package com.sprint.mission.discodeit.entity;


import lombok.Getter;

@Getter
public class User extends AllApply {
	
	String name;
	String email;
	
	public User(String name, String email) {
		super();
		this.name = name;
		this.email = email;
	}
	
	public void renew(String name, String email) {
		this.name = name;
		this.email = email;
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
