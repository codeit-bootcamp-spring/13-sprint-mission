package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
@Entity
@Table(name = "channels")
public class Channel extends BaseUpdatableEntity{

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ChannelType type;

    @Column(length = 500)
    private String description;

    protected Channel() {}

    public Channel(ChannelType type, String name, String description){
        super();
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public void updateChannel(String name) {
        this.name = name;
    }

    public void updateChannelDescription(String description) {
        this.description = description;
    }
}
