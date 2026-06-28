package com.sprint.mission.discodeit.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//사용자 생성 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

  private String username; //사용자 이름
  private String email; //사용자 이메일 주소
  private String password; //사용자 비밀번호
}
