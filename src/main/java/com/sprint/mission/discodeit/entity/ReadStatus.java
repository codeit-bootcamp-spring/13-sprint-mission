package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusUpdateCommand;
import com.sprint.mission.discodeit.entity.base.UpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "read_statuses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"})
)
@ToString(exclude = {"user", "channel"}, callSuper = true)
public class ReadStatus extends UpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled;

    public ReadStatus(Channel channel, User user, ReadStatusCreateCommand command) {
        this(channel, user, command.readAt());
    }

    public ReadStatus(Channel channel, User user, Instant lastReadAt) {
        this.user = user;
        this.channel = channel;
        this.lastReadAt = lastReadAt;
        this.notificationEnabled = channel.isPrivate();
    }

    /**
     * 요청에 포함된 필드만 변경한다.
     * 생략된 null 필드는 유지하며 알림 설정의 false 값은 유효한 변경으로 적용한다.
     */
    public void updateInfo(ReadStatusUpdateCommand command) {
        if (command.readAt() != null){
            this.lastReadAt = command.readAt();
        }
        if (command.newNotificationEnabled() != null){
            this.notificationEnabled = command.newNotificationEnabled();
        }
    }

    public UUID getUserId() {
        if (user == null) return null;
        return user.getId();
    }

    public UUID getChannelId() {
        if (channel == null) return null;
        return channel.getId();
    }

}
