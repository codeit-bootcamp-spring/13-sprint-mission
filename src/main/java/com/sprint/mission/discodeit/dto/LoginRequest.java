package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

// 로그인 요청 DTO
// 사용자가 로그인할 때 입력한 username과 password를 담는 클래스
@Getter
public class LoginRequest {

    // 로그인할 사용자 이름
    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;

    // 로그인할 비밀번호
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    // 로그인 요청 객체를 생성하는 생성자
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}