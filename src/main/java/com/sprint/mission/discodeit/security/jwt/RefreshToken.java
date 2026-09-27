package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.entity.BaseEntity;
import com.sprint.mission.discodeit.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken extends BaseEntity {
    @Column(nullable = false,unique = true)
    private String hash;

    @Column(nullable = false)
    private Instant expire;

    @ManyToOne()
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column
    private State state;

    public enum State {
        ROTATED,
        REVOKED
    }

    public RefreshToken(String hash, Instant expire, User user) {
        this.hash = hash;
        this.expire = expire;
        this.user = user;
    }

    public void revoke(){ state = State.REVOKED; }
}
