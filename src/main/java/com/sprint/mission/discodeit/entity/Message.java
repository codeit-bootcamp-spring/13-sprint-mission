package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;
import static java.util.UUID.randomUUID;

public class Message implements Serializable {
    private static final long serialVersionUID=1L;
    // 직렬화 및 역직렬화를 수행할 때 이 클래스의 버전을 의미
    private final UUID id; // 객체 식별
    // UUID 범용 고유 식별자, 중복 되지 않는 유일한 값
    private final Long createdAt;
    private Long updatedAt; // 각각 객체의 생성, 수정 시간을 유닉스 타임스탬프로 나타냄
    private String content;
    private UUID channelId;

    // 생성자 호출
    public Message(UUID channelId, String content, Long createdAt) {
        this(randomUUID(), channelId, content, System.currentTimeMillis());
    }

    public Message(UUID id, UUID channelId, String content, Long createdAt) {
        this.id=id; // id 초기화
        this.channelId=normalizeChannelId(channelId);
        this.content=normalizeContent(content);
        this.createdAt= createdAt; // 유낙스 타임스탬프 얻기
        this.updatedAt=createdAt; // 우선 생성시점과 동일하게 초기화
    }

    // 필드 수정하는 update 함수 정의
    public void updateMessage(UUID newChannelId, String newContent, Long updatedAt) {
        this.channelId=normalizeChannelId(newChannelId);
        this.content=normalizeContent(newContent);
        this.updatedAt=System.currentTimeMillis();
        System.out.println("최초생성: "+createdAt+"\n채널: "+newChannelId+"\n메시지내용: "+newContent+"\n수정: "+updatedAt);
        System.out.println();
    }

    private String normalizeContent(String newContent){
        if(newContent.isBlank()){
            return "조심하세요! 공백은 불가능합니다...";
        }
        return newContent;
    }

    private UUID normalizeChannelId(UUID newChannelId){
        if(newChannelId==null) {
            System.out.println("조심하세요! 공백은 불가능합니다....");
            return channelId;
        }
        return newChannelId;
    }

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
