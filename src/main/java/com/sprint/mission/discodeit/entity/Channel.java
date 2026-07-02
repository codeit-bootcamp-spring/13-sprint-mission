package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "channels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseUpdatableEntity {
    @Column(length = 100)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ChannelType type;
}