package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserDto {

  // 패스워드 정보 제외, 온라인 상태 정보 포함
  private UUID id;
  private Instant createdAt;
  private Instant updatedAt;
  private String username;
  private String email;
  private UUID profileId;
  private Boolean online; // 엔티티 자체가 아닌 온라인 여부인 필요한 값만 받자

  public static UserDto from(User user, UserStatus userStatus) {
    return UserDto.builder()
        .id(user.getId())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .username(user.getUsername())
        .email(user.getEmail())
        .profileId(user.getProfileId())
        .online(userStatus.isOnline())
        .build();
  }
}
