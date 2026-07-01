package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
//사용자가 채널별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델(사용자별 각 채널에 읽지 않은 메시지를 확인하기 위해 활용됨)
public class ReadStatus implements Serializable {
    private static final long SERIAL_VERSION_UID = 1L;
    private final UUID id; //읽을 상태의고유 식별자
    private UUID userId; //읽을 상태를 가진 사용자ID
    private UUID channelId; //채널 ID

    private Instant lastReadAt; //마지막으로 메시지를 읽은 시각

    private Instant updatedAt; //읽을 상태 정보 수정 시각
    private final Instant createdAt; //읽을 상태 정보 생성 시각

    public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {

        this.id = UUID.randomUUID(); //읽을 상태의 고유 ID 생성
        this.userId = userId; //사용자 ID 저장
        this.channelId = channelId; //채널 ID 저장
        this.lastReadAt = lastReadAt; //마지막 읽은 시각 저장
        this.updatedAt = Instant.now(); //수정 시각 초기화
        this.createdAt = Instant.now(); //생성 시각 초기화
    }

    public void update(Instant newLastReadAt) { //마지막 읽은 시각 수정 메서드
        boolean anyValueUpdated = false; //실제 수정 여부 확인용 변수
        if (newLastReadAt != null && !newLastReadAt.equals(lastReadAt)) { //새로운 읽은 식가이 존재하고 기존 값과 다를경우 수정수행
            this.lastReadAt = newLastReadAt; //마지막 읽은 시각 변경
            anyValueUpdated = true; //수정 발생 표시
        }
        if (anyValueUpdated) { //실제 변경이 발생한 경우에만 수정 시각 갱신
            this.updatedAt = Instant.now();
        }
    }

}
