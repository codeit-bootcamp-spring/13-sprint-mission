package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.*;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "channels")
public class Channel extends BaseUpdatableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false)
    private ChannelType type;
    private String description;
    private String name;

    public Channel(String name, String description, ChannelType type) {
        super();

        this.type = type;
        this.name = name;
        this.description = description;
    }


    public void update(String newName, String newDescription) {
        if (newName != null && newName.equals(this.name)) {
            this.name = newName;
        }

        if (newDescription != null && newDescription.equals(this.description)) {
            this.description = newDescription;
        }
    }
}
