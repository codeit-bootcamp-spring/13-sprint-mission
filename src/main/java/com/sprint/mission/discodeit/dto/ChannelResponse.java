package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 채널 응답 DTO
@Getter
public class ChannelResponse {

    private UUID id;

    private Instant createdAt;

    private Instant updatedAt;

    private ChannelType type;

    private String name;

    private String description;

    private Instant lastMessageAt;

    // PRIVATE 채널에 참여한 사용자 id 목록
    private List<UUID> participantUserIds;

    // 프론트 호환용 id 목록
    private List<UUID> participantIds;

    /*
     * 중요:
     * 프론트는 participants를 UUID 목록이 아니라 UserResponse 목록으로 기대한다.
     * 비공개 채널의 상대방 이름/프로필 이미지를 여기서 읽는다.
     */
    private List<UserResponse> participants;

    // 기존 생성자 유지
    public ChannelResponse(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            ChannelType type,
            String name,
            String description,
            Instant lastMessageAt,
            List<UUID> participantUserIds
    ) {
        this(
                id,
                createdAt,
                updatedAt,
                type,
                name,
                description,
                lastMessageAt,
                participantUserIds,
                List.of()
        );
    }

    // participants까지 받는 생성자
    public ChannelResponse(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            ChannelType type,
            String name,
            String description,
            Instant lastMessageAt,
            List<UUID> participantUserIds,
            List<UserResponse> participants
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.type = type;
        this.name = name;
        this.description = description;
        this.lastMessageAt = lastMessageAt;

        List<UUID> safeParticipantUserIds = participantUserIds == null
                ? new ArrayList<>()
                : participantUserIds;

        this.participantUserIds = safeParticipantUserIds;
        this.participantIds = safeParticipantUserIds;
        this.participants = participants == null ? List.of() : participants;
    }
}