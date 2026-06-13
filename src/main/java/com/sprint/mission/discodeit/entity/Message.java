package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

// ##메시지 정보를 담는 클래스
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id; //고유번호
    private Long createdAt; //만든시각
    private Long updatedAt;  //바뀐시각
    private String content; //메시지 내용
    private UUID channelId; //메시지가 속한 채널 id
    private UUID authorId; //메시지를 보낸 사용자 id

    // (참고?) 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화
    //생성자: 내용, 사용자id, 채널id를 받아서 Message 객체를 만듬
    public Message(String content, UUID channelId, UUID authorId) {
        this.id = UUID.randomUUID(); //고유 id 자동생성
        //this.createdAt = System.currentTimeMillis(); //생성시점 자동 기록 (밀리초 단위)
        this.createdAt = Instant.now().toEpochMilli(); // ^| (Instant.now().getEpochSecond(); 초(s)(단위로)

        this.content = content; //메시지 내용 저장
        this.authorId = authorId; //누가 쓴 건지
        this.channelId = channelId; //어디에 쓴 건지
    }

    //각 필드에 반환하는 getter함수 (정보를 꺼내는 함수)
    public UUID getId() {return id;}
    public Long getCreatedAt() {
        return createdAt;
    }
    public Long getUpdatedAt() {
        return updatedAt;
    }
    public String getContent() {return content;}
    public UUID getAuthorId() {return authorId;}
    public UUID getChannelId() {return channelId;}

    //메시지 내용을 바꾸는 update함수 (수정 시 update도 갱신)
    public void update(String newContent) {
        boolean anyValueUpdated = false;
        this.updatedAt = Instant.now().toEpochMilli();
        if (newContent != null && !newContent.equals(this.content)) { // 메시지 내용 저장, 조건(1. null이 아니어야함. 2.기존내용과 달라야함)
            this.content = newContent; //메시지 내용 변경
            anyValueUpdated = true; //수정 발생 표시
        }
        if (anyValueUpdated) { //실제 수정 발생 시 수정시간을 현재 시각으로 변경
            this.updatedAt = Instant.now().toEpochMilli();//객체를 밀리초 단위로 변환 (예 1970-01-01 00:00:00 UTC 의미함 (1749636000123))
            // this.updatedAt = Instant.now().getEpochSecond(); // 객체를 초(Second) 단위로 변환 (예 1970-01-01 00:00:00 UTC 의미함 (1749636000))
        }
    }
}

