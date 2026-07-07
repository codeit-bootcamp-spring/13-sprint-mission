package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class Channel extends BaseUpdatableEntity {

    private UUID id;
    private String channelTitles;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isPrivate;
    private List<UUID> userIds;

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

    public Channel(String channelTitles, String description, boolean isPrivate) {
        this.id = UUID.randomUUID();
        this.channelTitles = channelTitles;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.description = description;
        this.isPrivate = isPrivate;
        this.userIds = new ArrayList<>();
    }

    public void assignUsers(List<UUID> userIds) {
        if (userIds != null) {
            this.userIds.addAll(userIds);
        }
    }

    public void updateTitles(Channel channel) {
        this.channelTitles = channel.getChannelTitles();
        this.updatedAt = Instant.now();
    }

}