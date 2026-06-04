package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.time.*;
import java.util.*;

// 사용자 별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델
// 사용자의 온라인 상태를 확인하기 위해 활용
// 마지막 접속 시간을 기준으로 현재 로그인한 유저로 판단할 수 있는 메소드를 정의하세요.
// 마지막 접속 시간이 현재 시간으로부터 5분 이내이면 현재 접속 중인 유저로 간주합니다.

public class UserStatus extends BaseEntity {

    private final UUID userId;
    private Instant lastOnlineAt;


    public UserStatus(UUID userId) {
        super();
        if (userId == null) {
            throw new IllegalArgumentException("유저아이디가 생성되지 않았습니다");
        }
        this.userId = userId;
        this.lastOnlineAt = null;
    }


    public void setLastOnlineAt() {
        if(isOnline()) {
            this.lastOnlineAt = Instant.now();
        }
        setUpdatedAt();
    }


    public boolean isOnline() {
        if (lastOnlineAt == null) return false;
        Instant now = Instant.now();
        return !lastOnlineAt.isBefore(now.minus(Duration.ofMinutes(5)));
    }


}
