package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ChannelResponse { // record 타입은 실무에서 많이 사용되지 않기 때문에 전통적인 방식으로 DTO 선언
        private UUID id;
        private ChannelType type;
        private String name;
        private String description;
        private Instant lastMessageAt;
        private List<UUID> participantsIds; // PRIVATE 채널인 경우 참여하는 사용자의 아이디라는 의미를 명확히 하기 위해 participantsIds 로 변경

    public static ChannelResponse from(Channel channel, Instant lastMessageAt, List<UUID> participantsIds) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .type(channel.getType())
                .name(channel.getName())
                .description(channel.getDescription())
                .lastMessageAt(lastMessageAt)
                .participantsIds(participantsIds)
                .build(); // PRIVATE 채널인 경우, 참여한 user의 id 정보 포함
    }
}
