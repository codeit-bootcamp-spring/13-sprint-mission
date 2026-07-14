package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "channels")
public class Channel extends BaseUpdatableEntity {

    private UUID id;
    private String channelTitles;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isPrivate;
    private List<UUID> userIds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type;

    private String name;
    private String description;

    public Channel(String channelTitles, String description) {
        this.id = UUID.randomUUID();
        this.channelTitles = channelTitles;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.isPrivate = false;
        this.userIds = new ArrayList<>();
    }

    public Channel(String channelTitles, String description, ChannelType type) {
        this.id = UUID.randomUUID();
        this.channelTitles = channelTitles;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.userIds = new ArrayList<>();
        this.type = type;
    }

    public void assignUsers(List<UUID> userIds) {
        if (userIds != null) {
            this.userIds.addAll(userIds);
        }
    }

    public void updateTitles(String name, String description) {
        this.name = name;
        this.description = description;
    }

}