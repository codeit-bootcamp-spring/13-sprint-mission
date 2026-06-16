package com.sprint.mission.discodeit.dto.request;

public record ChannelPublicRequest(
        String name,
        String description
) {}

/*
PUBLIC 채널을 생성할 때에는 기존 로직을 유지합니다.
[ ] name과 description 속성이 필요합니다.
[ ] 채널에 참여하는 User의 정보를 받아 User 별 ReadStatus 정보를 생성합니다.
 */