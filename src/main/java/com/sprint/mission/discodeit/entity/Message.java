package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

// ##메시지 정보를 담는 클래스
public class Message implements Serializable {
    private UUID id; //고유번호
    private Long createdAt; //만든시각
    private Long updatedAt;  //바뀐시각
    private String content; //메시지 내용
    private UUID userId; //메시지를 보낸 사용자 id
    private UUID channelId; //메시지가 속한 채널 id

    // (참고?) 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화
    //생성자: 내용, 사용자i, 채널id를 받아서 Message 객체를 만듬
    public Message(String content, UUID userId, UUID channelId) {
        this.id = UUID.randomUUID(); //고유 id 자동생성
        this.createdAt = System.currentTimeMillis(); //생성시점 자동 기록
        this.updatedAt = this.createdAt; //처음엔 생성시각과 같음
        this.content = content; //메시지 내용
        this.userId = userId; //누가 쓴 건지
        this.channelId = channelId; //어디에 쓴 건지
    }

    //각 필드에 반환하는 getter함수 (정보를 꺼내는 함수)
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

    //메시지 내용을 바꾸는 update함수 (수정 시 update도 갱신)
    public void updateContent(String newContent) {
        this.content = newContent; //내용 바꿈.
        this.updatedAt = System.currentTimeMillis(); //수정시각도 최신으로
    }
}