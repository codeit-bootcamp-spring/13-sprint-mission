package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public abstract class AllApply {
	
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
	} // 현재시간 갱신
	
}
