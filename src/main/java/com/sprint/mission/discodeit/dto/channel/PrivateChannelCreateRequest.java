package com.sprint.mission.discodeit.dto.channel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@ToString
public class PrivateChannelCreateRequest { // name, description 속성 생략, record 타입은 실무에서 많이 사용되지 않기 때문에 전통적인 방식으로 DTO 선언
    private List<UUID> participantsIds; // 채널 참여하는 사용자들의 아이디 명확히 표현하기 위해 participantsIds 로 표현
}
