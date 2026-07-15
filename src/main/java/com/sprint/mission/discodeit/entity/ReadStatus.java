package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "read_statuses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_read_statuses_user_channel",
                        columnNames = {"user_id", "channel_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

    // 어떤 사용자의 읽음 상태인지 참조하기 위한 User의 id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // 어떤 채널에 대한 읽음 상태인지 참조하기 위한 Channel의 id
    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    // 해당 사용자가 해당 채널을 마지막으로 읽은 시간
    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    // 생성자: 사용자 id와 채널 id를 받아 ReadStatus 객체를 생성
    public ReadStatus(UUID userId, UUID channelId) {
        validate(userId, channelId);

        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = Instant.now();
    }

    // 마지막 읽은 시간을 현재 시간으로 갱신하는 메서드
    public void updateLastReadAt() {
        Instant now = Instant.now();

        this.lastReadAt = now;
        markUpdated();
    }

    // 필요하면 특정 시간으로 갱신할 때 사용
    public void updateLastReadAt(Instant lastReadAt) {
        if (lastReadAt == null) {
            throw new IllegalArgumentException("마지막 읽은 시간은 비어 있을 수 없습니다.");
        }

        this.lastReadAt = lastReadAt;
        markUpdated();
    }

    private void validate(UUID userId, UUID channelId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 비어 있을 수 없습니다.");
        }

        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 비어 있을 수 없습니다.");
        }
    }
}