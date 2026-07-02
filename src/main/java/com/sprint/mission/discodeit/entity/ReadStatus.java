package com.sprint.mission.discodeit.entity;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ReadStatus extends BaseUpdatableEntity {
    private final User user;
    private final Channel channel;
    private Instant lastReadAt;

    // restore
    public ReadStatus(
            UUID id,
            Instant ctime,
            Instant mtime,
            User user,
            Channel channel) {
        super(id, ctime, mtime);
        this.user = user;
        this.channel = channel;
        this.lastReadAt = ctime;
    }

    // generator
    public ReadStatus(User user) {
        this.user = user;
        this.lastReadAt = Instant.now();
    }



}
