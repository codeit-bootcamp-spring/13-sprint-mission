package com.sprint.mission.discodeit.dto.request;

import java.util.List;
import java.util.UUID;

public record ChannelPrivateRequest(
        List<UUID> channelIds
) {}

/*
PRIVATE 채널을 생성할 때:
[ ] 채널에 참여하는 User의 정보를 받아 User 별 ReadStatus 정보를 생성합니다.
[ ] name과 description 속성은 생략합니다.
 */