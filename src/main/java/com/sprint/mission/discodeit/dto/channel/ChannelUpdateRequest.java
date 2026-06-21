package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class ChannelUpdateRequest { // record 타입은 실무에서 많이 사용되지 않기 때문에 전통적인 방식으로 DTO 선언
    private ChannelType newType;
    private String newName;
    private String newDescription;
}
