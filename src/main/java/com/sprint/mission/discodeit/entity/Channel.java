package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class Channel extends BaseUpdatableEntity{

    private String name;
    private ChannelType type;
    private String description;

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
