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
    @Column(nullable = false)
    private ChannelType type;
    private String description;
    private String name;

    public Channel(String name, String description, ChannelType type) {
        super();

        this.type = type;
        this.name = name;
        this.description = description;
    }

}
