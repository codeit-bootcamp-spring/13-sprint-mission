package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest( // name, description 속성 생략
        List<UUID> userIds // 채널 참여하는 사용자들의 아이디
) { }
