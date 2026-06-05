package com.sprint.mission.discodeit.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

// memo - class ID 가 달라졌었음. 필드 받아가는 로직을 Getter로 바꾼것 뿐인데 왜?


@Getter
@Setter
@AllArgsConstructor
public class Message extends BaseEntity {
    private final UUID userID;
    private final UUID channelID;
    private String text;
    private final Set<UUID> attrID;

    @Override
    public String toString() {
        return "Message{" +
                "userID=" + userID +
                ", channelID=" + channelID +
                ", text='" + text + '\'' +
                ", attrID=" + attrID +
                '}';
    }
}
