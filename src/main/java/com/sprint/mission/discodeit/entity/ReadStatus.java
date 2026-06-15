package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ReadStatus extends BaseEntity {
    private final UUID userID;
    private final UUID channelID;

    @Override
    public String toString() {
        return "ReadStatus{" +
                "userID=" + userID +
                ", channelID=" + channelID +
                '}';
    }
}
