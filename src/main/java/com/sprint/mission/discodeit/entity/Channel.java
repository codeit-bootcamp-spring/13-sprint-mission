package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class Channel extends BaseUpdatableEntity {

    //필드
    private ChannelType type;
    private String name;
    private String description;

    //ctor
    public Channel(ChannelType type) {
        super();

        this.type = type;
        this.name = "private Channel";
        this.description = "private Channel";
    }

    public Channel(ChannelType type, String name, String description) {
        super();

        this.type = type;
        this.name = name;
        this.description = description;
    }

    //update Method
    public void updateChannel(String name, String description) {
        this.name = name;
        this.description = description;

        updateUpdatedAt();
    }

    //method override
    @Override
    public String toString() {
        return "[Channel: " + name + ", Description: " + description + "]";
    }
}