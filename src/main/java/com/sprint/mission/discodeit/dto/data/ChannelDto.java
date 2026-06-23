package com.sprint.mission.discodeit.dto.data;

import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

//채널 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDto {
        private UUID id; //시스템 내에서 채널을 유일하게 식별하는 값
        private String name; //채널이름
        private String description; //채널설명
        private ChannelType type; //채널유형
        private Instant lastMessageAt; //마지막 메시지가 작성된 시각
        private List<UUID> participantsIds; //채널 참가자들의 UUID 목록
}
