package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import static java.util.UUID.randomUUID;

@Getter // 도메인 모델의 getter 메소드를 @Getter로 대체
public class Channel implements Serializable {

    @Serial
    private static final long serialVersionUID=1L;
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt; // 시간 다루는 필드 타입은 Instant로 바꾸어 가독성과 확장성 확보
    private ChannelType type;
    private String name, description; // 채널 설명 추가

    // 객체 생성 시 클래스 외부에서 정의해야 하는 값만 파라미터로 정의
    public Channel(ChannelType type, String name, String description){
        this.id= randomUUID();
        this.createdAt= Instant.now();
        this.updatedAt= Instant.now();
        this.type=type;
        this.name=name;
        this.description=description;
    }

    public Channel(ChannelType channelType) {
        this(channelType, "", "");
    }

    // 필드 수정하는 update 함수 정의
    public void update(ChannelType newType, String newName, String newDescription){
        boolean anyValueUpdated=false; // updatedat은 실제 변경이 있을 때만 갱신되도록 구성
        if (newType != null && !newType.equals(this.type)) { // newType이 기존값과 다를 때만 업데이트 되도록 한다
            this.type = newType;
            anyValueUpdated = true;
        }
        if (newName != null && !newName.equals(this.name)) {
            this.name = newName;
            anyValueUpdated = true;
        }
        if (newDescription != null && !newDescription.equals(this.description)) { // null 체크 필요
            this.description=newDescription;
            anyValueUpdated=true;
        }
        if (!anyValueUpdated){
            throw new IllegalArgumentException("변경사항이 없습니다!");
        }
        this.updatedAt=Instant.now();
    }

}
