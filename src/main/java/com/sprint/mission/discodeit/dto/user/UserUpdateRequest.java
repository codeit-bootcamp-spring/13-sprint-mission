package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "수정할 User 정보")
public class UserUpdateRequest { // 수정 대상 객체의 id 파라미터, 수정할 값 파라미터

  private String newUsername;
  private String newEmail;
  private String newPassword;
}
