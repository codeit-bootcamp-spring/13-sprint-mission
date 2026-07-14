package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "read_statuses")
public class ReadStatus extends BaseUpdatableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private UUID channelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    private Instant lastReadAt;

    public ReadStatus(Channel channel, UUID userId) {
        this.userId = userId;
        this.channelId = channelId;
        this.channel = channel;
        this.lastReadAt = Instant.now();
    }

    public void updateLastRead() {
        this.lastReadAt = Instant.now();
    }

}