package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Channel extends BaseUpdatableEntity {
    private String name;
    private String description;
    private ChannelType type;

    // restore
    public Channel(
            UUID id,
            Instant ctime,
            Instant mtime,
            String name,
            String description,
            ChannelType type) {
        super(id, ctime, mtime);
        this.name = name;
        this.description = description;
        this.type = type;
    }

    // generator
    public Channel(
            String name,
            String description,
            ChannelType type
    ){
        super();
        this.name = name;
        this.description = description;
        this.type = type;
    }

}