package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
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

    // 읽음 상태를 가진 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 읽음 상태가 적용되는 채널
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    // 해당 사용자가 해당 채널을 마지막으로 읽은 시간
    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    public ReadStatus(User user, Channel channel) {
        validate(user, channel);

        this.user = user;
        this.channel = channel;
        this.lastReadAt = Instant.now();
    }

    public void updateLastReadAt() {
        this.lastReadAt = Instant.now();
        markUpdated();
    }

    public void updateLastReadAt(Instant lastReadAt) {
        if (lastReadAt == null) {
            throw new IllegalArgumentException("마지막 읽은 시간은 비어 있을 수 없습니다.");
        }

        this.lastReadAt = lastReadAt;
        markUpdated();
    }

    public UUID getUserId() {
        return user == null ? null : user.getId();
    }

    public UUID getChannelId() {
        return channel == null ? null : channel.getId();
    }

    private void validate(User user, Channel channel) {
        if (user == null) {
            throw new IllegalArgumentException("사용자는 비어 있을 수 없습니다.");
        }

        if (channel == null) {
            throw new IllegalArgumentException("채널은 비어 있을 수 없습니다.");
        }
    }
}