package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Getter
@Entity
@Table(
        name = "read_statuses",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "channel_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled;

    public ReadStatus(User user, Channel channel) {
        this(user, channel, Instant.now());
    }

    public ReadStatus(
            User user,
            Channel channel,
            Instant lastReadAt
    ) {
        this.user = Objects.requireNonNull(
                user,
                "사용자는 필수입니다."
        );
        this.channel = Objects.requireNonNull(
                channel,
                "채널은 필수입니다."
        );
        this.lastReadAt = Objects.requireNonNull(
                lastReadAt,
                "마지막 읽은 시간은 필수입니다."
        );

        this.notificationEnabled =
                channel.getType() == ChannelType.PRIVATE;
    }

    public void update(
            Instant lastReadAt,
            Boolean notificationEnabled
    ) {
        if (lastReadAt != null
                && !lastReadAt.equals(this.lastReadAt)) {
            this.lastReadAt = lastReadAt;
        }

        if (notificationEnabled != null) {
            this.notificationEnabled = notificationEnabled;
        }
    }
}