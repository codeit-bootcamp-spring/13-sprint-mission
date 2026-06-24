package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static java.util.UUID.randomUUID;

@Getter
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    private final UUID id;
    private final UUID channelId;
    private final UUID authorId; // User 도메인 모델의 id와 연결하기 위해 authorId 추가
    private final Instant createdAt;
    private Instant updatedAt;
    private String content;
    private List<UUID> attachmentIds;

    public Message(String content, UUID channelId, UUID authorId, List<UUID> attachmentIds) {
        this.id=randomUUID();
        this.createdAt= Instant.now();
        this.updatedAt= Instant.now();
        this.content=content;
        this.channelId=channelId; // 채널 ID는 수정할 수 없는 값
        this.authorId=authorId;
        this.attachmentIds = attachmentIds;
    }

    // 필드 수정하는 update 함수 정의
    public void update(String newContent) {
        boolean anyValueUpdated=false; // 수정시간은 실제 변경이 있을 때만 갱신되도록 구성
        if(newContent != null && !newContent.equals(this.content)){
            this.content=newContent;
            anyValueUpdated=true;
        }
        if (!anyValueUpdated) {
            throw new IllegalArgumentException("변경사항이 없습니다!");
        }
        this.updatedAt=Instant.now();
    }

}
