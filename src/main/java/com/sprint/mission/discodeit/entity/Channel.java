package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Channel extends BaseEntity {
    private String name;
    private String description;
    private ChannelType type;
}