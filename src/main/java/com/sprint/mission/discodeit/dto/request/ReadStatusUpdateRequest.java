package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

//읽을 상태 생성 수정 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadStatusUpdateRequest{
        private Instant newLastReadAt; //수정할 새로운 마지막 읽을 시각
}
