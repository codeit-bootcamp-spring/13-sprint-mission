package com.sprint.mission.discodeit.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//사용자 정보 수정 요청을 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

  private String newUsername; //수정할 새로운 사용자명(기존 username을 변경할 때 사용됨)
  private String newEmail; //수정할 새로운 이메일 주소
  private String newPassword; //수정할 새로운 비밀번호
}
