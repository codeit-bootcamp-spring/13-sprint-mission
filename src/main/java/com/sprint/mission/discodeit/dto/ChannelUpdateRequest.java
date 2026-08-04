package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 채널 수정 요청 DTO
@Getter
@NoArgsConstructor
public class ChannelUpdateRequest {

    // 수정할 채널의 id
    private UUID id;

    // 기존 코드 호환용
    private ChannelType type;

    /*
     * 기존 프론트/기존 코드 호환용 필드
     * title, channelName으로 들어와도 name에 매핑
     */
    @Size(
            max = 100,
            message = "채널 이름은 100자 이하여야 합니다."
    )
    @Pattern(
            regexp = ".*\\S.*",
            message = "채널 이름은 공백으로만 구성할 수 없습니다."
    )
    @JsonAlias({"title", "channelName"})
    private String name;

    /*
     * 기존 프론트/기존 코드 호환용 필드
     * desc로 들어와도 description에 매핑
     */
    @Size(
            max = 500,
            message = "채널 설명은 500자 이하여야 합니다."
    )
    @JsonAlias({"desc"})
    private String description;

    /*
     * API 명세 v1.2 기준 필드
     */
    @Size(
            max = 100,
            message = "새 채널 이름은 100자 이하여야 합니다."
    )
    @Pattern(
            regexp = ".*\\S.*",
            message = "새 채널 이름은 공백으로만 구성할 수 없습니다."
    )
    private String newName;

    /*
     * API 명세 v1.2 기준 필드
     */
    @Size(
            max = 500,
            message = "새 채널 설명은 500자 이하여야 합니다."
    )
    private String newDescription;

    public ChannelUpdateRequest(
            UUID id,
            ChannelType type,
            String name,
            String description
    ) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.newName = name;
        this.newDescription = description;
    }

    /*
     * 서비스 코드에서는 getName()을 그대로 사용해도 됨.
     * newName이 있으면 newName 우선 사용.
     */
    public String getName() {
        if (newName != null && !newName.isBlank()) {
            return newName;
        }

        return name;
    }

    /*
     * 서비스 코드에서는 getDescription()을 그대로 사용해도 됨.
     * newDescription이 있으면 newDescription 우선 사용.
     */
    public String getDescription() {
        if (newDescription != null && !newDescription.isBlank()) {
            return newDescription;
        }

        return description;
    }

    @AssertTrue(message = "수정할 채널 이름 또는 설명이 필요합니다.")
    public boolean isUpdateValueProvided() {
        return name != null
                || description != null
                || newName != null
                || newDescription != null;
    }
}