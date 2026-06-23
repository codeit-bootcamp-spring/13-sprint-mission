package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

//읽을 상태 생성 요청 정보를 전달하기위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadStatusCreateRequest{
        private UUID userId; //어떤 사용자의 읽음 상태인지 구분하기 위해 사용됨.
        private UUID channelId; //어떤 채널에 대한 읽을 상태잍지 구분하기 위해 사용됨.
        private Instant lastReadAt; //사용자가 마지막으로 메시지를 읽은 시각
}
