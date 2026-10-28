package com.sprint.mission.discodeit.dto.request.readStatus;

import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusUpdateCommand;

import java.time.Instant;

/**
 * 읽음 시각과 알림 설정을 각각 선택적으로 바꾸는 부분 수정 요청이다.
 * null은 기존 값을 유지한다는 뜻이며, Boolean false는 알림 해제 요청으로 처리한다.
 */
public record ReadStatusUpdateRequest(
        Instant newLastReadAt,
        Boolean newNotificationEnabled
) {
    public ReadStatusUpdateCommand toCommand() {
        return new ReadStatusUpdateCommand(newLastReadAt, newNotificationEnabled);
    }
}