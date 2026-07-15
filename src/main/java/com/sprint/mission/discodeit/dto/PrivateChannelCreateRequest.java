package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias({"participantUserIds", "participants", "userIds", "memberIds"})
    private List<UUID> participantIds;

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