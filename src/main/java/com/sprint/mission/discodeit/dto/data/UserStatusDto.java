package com.sprint.mission.discodeit.dto.data;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter //모든 필드에 대한 Getter메서드 자동생성
@NoArgsConstructor //기본 생성자(UserDTO()) 자동생성
@AllArgsConstructor //모든 필드를 매개변수로 받는 생성자 자동 생성
public class UserStatusDto {

  private UUID id;
  private UUID userId;
  //private Boolean online; 할지안할지 정해보기?
  private LocalDateTime lastActive;
}
