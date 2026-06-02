package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message {
    private UUID id; //고유번호
    private Long createdAt;
    private Long updatedAt;  //만든시각,바뀐시각
    private String content;
    private UUID userId;
    private UUID channelId;

    // (참고?) 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화
    public Message(String content, UUID userId, UUID channelId) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.content = content;
        this.userId = userId;
        this.channelId = channelId;
    }

    //메시지 내용을 바꾸는 update함수
    public void updateContent(String newContent) {
        this.content = newContent; //내용 바꿈.
        this.updatedAt = System.currentTimeMillis(); //수정시각도 최신으로
    }

    //정보를 꺼내는 함수
    public UUID getId() {return id;}
    public long getCreatedAt() {
        return createdAt;
    }
    public long getUpdatedAt() {
        return updatedAt;
    }
    public String getContent() {return content;}
    public UUID getUserId() {return userId;}
    public UUID getChannelId() {return channelId;}
}