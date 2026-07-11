package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "channel")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseUpdatableEntity {

	@Column(name = "name", length = 100)
	private String name;

	@Column(name = "description", length = 500)
	private String nameDescription;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 10)
	private ChannelType type;

	
	
	public Channel(String name, String nameDescription, User creator, ChannelType type) {
		super();
		this.name = name;
		this.nameDescription = nameDescription;
		this.type = type;
	}

	public void update(String name, String nameDescription) {
		this.name = name;
		this.nameDescription = nameDescription;
	}

	@Override
	public String toString() {
		return "Channel[" +
				"id= " + getId() + "\n" +
				"Name=" + getName() + "\n" +
				"NameDescription=" + getNameDescription() + "\n" +
//				"Creator=" + (creator == null ? null : getCreator())+ "\n" +
				"Type=" + getType() + "\n" +
				"]";
	}

	
}
