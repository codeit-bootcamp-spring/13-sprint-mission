package com.sprint.mission.discodeit.entity;

public enum Role {
  USER("일반"), CHANNEL_MANAGER("매니저"), ADMIN("어드민");

  private final String tag;

  Role(String tag) {
    this.tag = tag;
  }
}
