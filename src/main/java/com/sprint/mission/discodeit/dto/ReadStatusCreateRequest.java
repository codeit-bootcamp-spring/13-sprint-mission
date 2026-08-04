package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

// ReadStatus 생성 요청 DTO
// 특정 User가 특정 Channel에 대한 읽음 상태를 생성할 때 필요한 데이터를 담는 클래스
@Getter
public class ReadStatusCreateRequest {

    // 읽음 상태를 생성할 사용자 id
    @NotNull(message = "사용자 ID는 필수입니다.")
    private UUID userId;

    // 읽음 상태를 생성할 채널 id
    @NotNull(message = "채널 ID는 필수입니다.")
    private UUID channelId;

    // ReadStatus 생성 요청 객체를 생성하는 생성자
    public ReadStatusCreateRequest(UUID userId, UUID channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }
}