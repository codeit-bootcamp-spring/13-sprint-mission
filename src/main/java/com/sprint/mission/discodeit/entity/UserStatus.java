package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

// 사용자 별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델
// 사용자의 온라인 상태를 확인하기 위해 활용
// 마지막 접속 시간을 기준으로 현재 로그인한 유저로 판단할 수 있는 메소드를 정의하세요.
// 마지막 접속 시간이 현재 시간으로부터 5분 이내이면 현재 접속 중인 유저로 간주합니다.

@Getter
public class UserStatus extends BaseEntity implements Serializable {

    private final UUID userId;
    private Instant lastOnlineAt;


    public UserStatus(UUID userId) {
        super();
        validateUserId(userId);
        this.userId = userId;
        this.lastOnlineAt = null;
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("유저아이디가 없습니다. 확인해주세요");
        }
    }


    public void markOnline() {
        this.lastOnlineAt = Instant.now();
        setUpdatedAt();
    }



    // 현재 사용자가 온라인에 접속중인지 확인하는 메서드
    public boolean isOnline() {
        if (lastOnlineAt == null) return false;
        Instant now = Instant.now();
        return !lastOnlineAt.isBefore(now.minus(Duration.ofMinutes(5)));
    }

    public void updateLastOnlineAt(Instant lastOnlineAt) {
        this.lastOnlineAt = lastOnlineAt;
        setUpdatedAt();
    }

}
