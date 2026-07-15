package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    public ReadStatus(User user, Channel channel, Instant lastReadAt) {
        this.user = user;
        this.channel = channel;
        this.lastReadAt = lastReadAt;
    }

    public void updateReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
