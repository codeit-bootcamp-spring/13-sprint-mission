package com.sprint.mission.discodeit.dto.response;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageDto(
        UUID id
        , Instant createaAt
        , Instant updateaAt
        , String content
        , UUID channelId
        , UserDto author
        , List<BinaryContentDto> attachments
        ) {
}
