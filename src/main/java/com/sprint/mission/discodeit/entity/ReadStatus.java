package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Getter
public class ReadStatus extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID userId;
    private final UUID channelId;
    private Instant lastReadTime;

    public ReadStatus(UUID userId, UUID channelId) {
        super();
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadTime = lastReadTime;
    }

    public void update(Instant lastReadTime) {
        boolean anyValueUpdated = false;
        if (lastReadTime != null && lastReadTime.equals(this.lastReadTime)) {
            this.lastReadTime = lastReadTime;
            anyValueUpdated = true;
        }

        if (anyValueUpdated) {
            setUpdatedAt();
        }

    }
}
