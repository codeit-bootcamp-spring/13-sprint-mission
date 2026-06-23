package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
//사용자별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델(사용자의 온란인 상태를 확인 하기 위해 활용됨)
public class UserStatus implements Serializable {
    private static final long SERIAL_VERSION_UID = 1L;
    private final UUID id; //사용자 상태 고유 식별자
    private UUID userId; //사용자 ID
    private Instant lastConnectedAt; //마지막 접속(활동) 시각
    private Instant updatedAt; //상태 정보 수정 시각
    private Instant createdAt; //상태 정보 생성 시각

    public UserStatus(UUID userId, Instant lastConnectedAt) {
        this.id = UUID.randomUUID(); //상태 객체 고유 ID 생성
        this.userId = userId; //사용자 ID 저장
        this.lastConnectedAt = Instant.now(); //마지막 접속 시간 저장
        this.updatedAt = Instant.now(); //수정 시각 초기화
        this.createdAt = Instant.now(); //생성 시각 초기화
    }

    public void update(Instant lastActiveAt) { //마지막 접속 시간 수정 메서드
        boolean anyValueUpdated = false; //실제 수정 여부 확인용
        if (lastActiveAt != null && !lastActiveAt.equals(this.lastConnectedAt)) { //새로운 시간이 존재하고 기존 값과 다를 경우 수정 수행
            this.lastConnectedAt = lastActiveAt;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) { //실제 수정이 발생한 경우 수정 시간 갱신
            this.updatedAt = Instant.now();
        }
    }

    //현재 온라인 여부 확인. 마지막 접속 시간이 5분 이내면 true
    public Boolean isOnline() {
        Instant instantFiveMinutesAgo = Instant.now().minus(Duration.ofMinutes(5)); //현재 시각 기준 5분전 시각 계산
        return lastConnectedAt.isAfter(instantFiveMinutesAgo); //마지막 접속 시간이 5분전 이후라면 온라인 상태
    }

}
