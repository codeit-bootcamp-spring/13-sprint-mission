package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "channels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseUpdatableEntity {

    // 채널 종류: PUBLIC / PRIVATE
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private ChannelType type;

    // 채널 이름
    // PRIVATE 채널은 이름을 사용하지 않을 수 있으므로 nullable 허용
    @Column(name = "name", length = 100)
    private String name;

    // 채널 설명
    // PRIVATE 채널은 설명을 사용하지 않을 수 있으므로 nullable 허용
    @Column(name = "description", length = 500)
    private String description;

    public Channel(ChannelType type, String name, String description) {
        validate(type, name, description);

        this.type = type;
        this.name = name;
        this.description = description;
    }

    public void update(ChannelType type, String name, String description) {
        validate(type, name, description);

        this.type = type;
        this.name = name;
        this.description = description;
        markUpdated();
    }

    private void validate(ChannelType type, String name, String description) {
        if (type == null) {
            throw new IllegalArgumentException("채널 종류는 비어 있을 수 없습니다.");
        }

        /*
         * PRIVATE 채널은 name, description을 사용하지 않는다.
         * 따라서 null이어도 허용한다.
         */
        if (type == ChannelType.PRIVATE) {
            return;
        }

        // PUBLIC 채널은 name 필수
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("채널 이름은 비어 있을 수 없습니다.");
        }

        // PUBLIC 채널은 description 필수
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("채널 설명은 비어 있을 수 없습니다.");
        }
    }
}