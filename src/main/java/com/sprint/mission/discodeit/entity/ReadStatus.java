package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.time.*;
import java.util.*;

// 사용자가 채널 별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델
// 사용자별 각 채널에 읽지 않은 메시지를 확인하기 위해 활용
@Getter
public class ReadStatus extends BaseEntity {

    private final UUID userId;
    private final UUID channelId;
    private Instant lastReadTime;

    public ReadStatus(UUID userId, UUID channelId) {
        super();
        validateUserId(userId);
        validateChannelId(channelId);

        this.userId = userId;
        this.channelId = channelId;
        this.lastReadTime = null;
    }

    private void validateUserId(UUID userId) {
        if (userId == null ) {
            throw new IllegalArgumentException("유저 아이디가 없습니다");
        }
    }

    private void validateChannelId(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 아이디가 없습니다. 확인해주세요");
        }
    }

    private void validateReadTime(Instant readTime) {
        if (readTime == null) {
            throw new IllegalArgumentException("읽음 시간은 null일 수 없습니다.");
        }
    }

    public void markAsRead(Instant readTime) {
        validateReadTime(readTime);
        this.lastReadTime = readTime;
        setUpdatedAt();
    }

    // 내가 읽은 시간 이후에 새로운 메세지가 왔는지 확인하는 메서드
    public boolean hasUnreadMessage(Instant latestMessageAt) {
        if (latestMessageAt == null) return false;

        if (lastReadTime == null) {
            return true;
        }
        return latestMessageAt.isAfter(lastReadTime);
    }
}
