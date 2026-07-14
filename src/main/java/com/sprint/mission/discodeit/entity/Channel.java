package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "channels")
@Getter
public class Channel extends BaseUpdatableEntity {

  @Enumerated(EnumType.STRING)
  private ChannelType type;
  private String name;
  private String description;

  public Channel() {
  }

  public Channel(ChannelType type, String chName, String description) {
    this.type = type;
    this.name = chName;
    this.description = description;
  }

  public void update(String chName, String chDescription) {

    if (chName != null && !chName.equals(this.name)) {
      this.name = chName;
    }
    if (chDescription != null && !chDescription.equals(this.description)) {
      this.description = chDescription;
    }
  }
}