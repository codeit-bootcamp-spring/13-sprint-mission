package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.UUID;

@Getter
public class User {
    private final String id;
    private final Long createdAt;
    private Long updatedAt;

    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }


    // Getter
    // 상단에 @Getter 를 입력해서 게터 작성은 안해도 됨
    /*
    public String getId() { return id; }
    public Long getCreatedAt() { return createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
     */

    // 수정
    public void update() {
        this.updatedAt = System.currentTimeMillis();
    }
}