package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

// PRIVATE 채널 생성 요청 DTO
@Getter
@NoArgsConstructor
public class PrivateChannelCreateRequest {

    /*
     * API 명세 v1.2 기준 필드명
     *
     * 요청 예시:
     * {
     *   "participantIds": ["uuid1", "uuid2"]
     * }
     */
    @NotEmpty(message = "PRIVATE 채널 참여자는 한 명 이상 필요합니다.")
    private List<@NotNull(message = "참여자 ID는 null일 수 없습니다.") UUID> participantIds;

    public PrivateChannelCreateRequest(List<UUID> participantIds) {
        this.participantIds = participantIds;
    }

    /*
     * 기존 서비스 코드 호환용 getter
     */
    public List<UUID> getParticipantUserIds() {
        return participantIds;
    }
}