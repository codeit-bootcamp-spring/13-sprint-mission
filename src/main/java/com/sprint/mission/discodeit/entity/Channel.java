package com.sprint.mission.discodeit.entity;


import lombok.*;

import java.io.*;

@Getter
public class Channel extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private ChannelType type;
    private String description;
    private String name;

    public Channel(String name, String description, ChannelType type) {
        super();

        this.type = type;
        this.name = name;
        this.description = description;
    }


    public void update(String newName, String newDescription) {
        boolean anyValueUpdated = false;
        if (newName != null && newName.equals(this.name)) {
            this.name = newName;
            anyValueUpdated = true;
        }

        if (newDescription != null && newDescription.equals(this.description)) {
            this.description = newDescription;
            anyValueUpdated = true;
        }

        if (anyValueUpdated) {
            setUpdatedAt();
        }
    }
}
