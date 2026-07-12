package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class UserDto {

  // 패스워드 정보 제외, 온라인 상태 정보 포함
  private UUID id;
  private String username;
  private String email;
  private BinaryContentDto profile;
  private Boolean online; // 엔티티 자체가 아닌 온라인 여부인 필요한 값만 받자
}
