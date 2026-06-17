package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel extends EntityRoot implements Serializable {

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