package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Getter
@Entity
@Table(name = "read_statuses", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "last_read_at",nullable = false)
    private Instant lastReadAt;

    public ReadStatus(User user, Channel channel) {
        super();
        this.user = user;
        this.channel = channel;
        this.lastReadAt = Instant.now();
    }


    public ReadStatus(User user, Channel channel, Instant lastReadAt) {
        super();
        this.user = user;
        this.channel = channel;
        this.lastReadAt = lastReadAt;
    }

    public void update(Instant lastReadTime) {
        if (lastReadTime != null && !lastReadTime.equals(this.lastReadAt)) {
            this.lastReadAt = lastReadTime;
        }
    }
}
