package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

	@Column(name = "username", nullable = false, unique = true, length = 50)
	private String name;

	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_id")
	private BinaryContent profile;
	
	public User(String name, String email, String password, BinaryContent profile) {
		super();
		this.name = name;
		this.email = email;
		this.password = password;
		this.profile = profile;
	}

	public void renew(String name, String email, String password, BinaryContent profile) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.profile = profile;
	}

	public BinaryContent getProfile() {
		return profile;
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
