package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//로그인 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest{
        private String username; //사용자 로그인 아이디
        private String password; //사용자 로그인 비밀번호
}
