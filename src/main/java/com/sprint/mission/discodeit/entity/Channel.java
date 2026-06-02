package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Channel {
    private final UUID id; //고유번호
    private final Long createdAt;
    private Long updatedAt;  //만든시각,바뀐시각
    private String name;

    // 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화
    public Channel(String name) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.name = name;
    }

    //정보 꺼내는 함수도 있어요
    public UUID getId() {return id;}
    public long getCreatedAt() {return createdAt;}
    public long getUpdatedAt() {return updatedAt;}
    public String getName() {return name;}

    public void updateName(String newName) {
        this.name = newName;
        this.updatedAt = System.currentTimeMillis();
    }
}
