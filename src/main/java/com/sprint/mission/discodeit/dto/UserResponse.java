package com.sprint.mission.discodeit.dto;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserResponse {

    // 사용자 id
    private UUID id;

    // 사용자 생성 시간
    private Instant createdAt;

    // 사용자 수정 시간
    private Instant updatedAt;

    // 사용자 이름
    private String username;

    // 사용자 이메일
    private String email;

    // 프로필 이미지로 연결된 BinaryContent의 id
    private UUID profileId;

    // 프론트 호환용 필드
    // profileId와 같은 값을 담음
    private UUID profileImageId;

    // 프론트가 user.profile 형태를 기대할 수 있어서 추가
    private BinaryContentResponse profile;

    // 현재 온라인 상태 여부
    private boolean online;

    // 기존 코드 호환용 생성자
    public UserResponse(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            String username,
            String email,
            UUID profileId,
            boolean online
    ) {
        this(
                id,
                createdAt,
                updatedAt,
                username,
                email,
                profileId,
                null,
                online
        );
    }

    // profile 객체까지 포함하는 생성자
    public UserResponse(
            UUID id,
            Instant createdAt,
            Instant updatedAt,
            String username,
            String email,
            UUID profileId,
            BinaryContentResponse profile,
            boolean online
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.username = username;
        this.email = email;
        this.profileId = profileId;
        this.profileImageId = profileId;
        this.profile = profile;
        this.online = online;
    }
}