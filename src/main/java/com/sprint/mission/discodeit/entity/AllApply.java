package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public abstract class AllApply implements Serializable {
	
	private final UUID id;
	private final Long createAt;
	private Long updateAt;
	
	public AllApply() {
		this.id = UUID.randomUUID();
		this.createAt = System.currentTimeMillis();
		this.updateAt = this.createAt;
		
	}
	
	public UUID getId() {
		return this.id;
	}
	public Long getCreateAt() {
		return this.createAt;
	}
	public Long getUpdateAt() {
		return this.updateAt;
	}
	
	public void updateTime() {
		this.updateAt = System.currentTimeMillis();
	}
	
	
}
