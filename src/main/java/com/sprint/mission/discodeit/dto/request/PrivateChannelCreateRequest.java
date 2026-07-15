package com.sprint.mission.discodeit.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PrivateChannelCreateRequest {

        private List<UUID> userIds;

}
