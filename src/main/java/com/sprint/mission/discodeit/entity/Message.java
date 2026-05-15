package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message {

    private final UUID id; // [요구사항] id는 생성자에서 초기화 // [요구사항] 내부에서 초기화
    private String content; // 이름은 밖에서 받아옴
    private final Long createdAt; // [요구사항] createdAt은 생성자에서 초기화 // [요구사항] 내부에서 초기화
    private Long updatedAt; // 처음 생성 시엔 수정 시간도 생성 시간과 같음
    private final UUID authorId;
    private final UUID channelId;

    public Message(String content, UUID authorId, UUID channelId) {
        this.id = UUID.randomUUID();
        this.content = content;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.authorId = authorId;
        this.channelId = channelId;
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

    public UUID getAuthorId() {
        return authorId;
    }

}

/*
[ ] 등록 -> 메세지 전송
[ ] 조회(단건, 다건) -> 메세지 검색(특정 메세지 검색/전체 메세지 로딩)
[ ] 수정 -> 메세지 내용 수정
[ ] 수정된 데이터 조회 -> 메세지 재검색
[ ] 삭제 -> 메세지 삭제
[ ] 조회를 통해 삭제되었는지 확인 -> 메세지 재검색
 */