package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public record ChannelRequest(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID channelId,
        String name,
        String description,
        ChannelType type,
        Instant lastReadTime
) {

    public record CreatePublicChannel(
            String name,
            String description
    ) { }

    public record CreatePrivateChannel(UUID userId) {

    }

    public record FindById(UUID id){
    }

    public record FindAll(){
    }

    public record Update(
            UUID id,
            String name,
            String description,
            ChannelType type
    ){
    }

    public record Delete(UUID id){
    }

}
