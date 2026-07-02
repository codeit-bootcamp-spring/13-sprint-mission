package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name= "user_statuses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {
    @OneToOne
    @JoinColumn(name = "user_id",unique = true)
    private User user;
    @Column(nullable = false)
    private Instant lastActiveAt;
    // activation timeout ( 5min )
    private final Integer timeout = 5 * 60 * 1000;
    public boolean online(){
        return timeout > Duration.between(lastActiveAt, Instant.now()).abs().toMillis();
    }
}
