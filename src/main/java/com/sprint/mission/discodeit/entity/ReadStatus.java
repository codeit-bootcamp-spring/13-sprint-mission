package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Getter
@Table(name = "read_statuses", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"}))
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Channel channel;

    @Column(nullable = false)
    private Instant lastReadAt;

    public ReadStatus(User user, Channel channel) {
        super();
        this.user = user;
        this.channel = channel;
        this.lastReadAt = Instant.now();
    }

    public void updateLastReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
