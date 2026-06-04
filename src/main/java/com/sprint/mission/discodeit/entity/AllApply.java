package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public abstract class AllApply implements Serializable {
	
	private final UUID id;
	private final Long createAt;
	private Long updateAt;
	
	public AllApply() {
		this.id = UUID.randomUUID();
		this.createAt = System.currentTimeMillis();
		this.updateAt = this.createAt;
		
	}

	public void updateTime() {
		this.updateAt = System.currentTimeMillis();
	}
	
	
}
