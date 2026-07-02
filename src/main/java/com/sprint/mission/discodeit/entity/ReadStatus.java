package com.sprint.mission.discodeit.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "read_statuses")
public class ReadStatus extends BaseUpdatableEntity {
    @ManyToOne(cascade = CascadeType.REMOVE,optional = false)
    @JoinColumn(name="user_id")
    private User user;
    @ManyToOne(cascade = CascadeType.REMOVE,optional = false)
    @JoinColumn(name="channel_id")
    private Channel channel;
    @Column(nullable = false)
    private Instant lastReadAt;
}
