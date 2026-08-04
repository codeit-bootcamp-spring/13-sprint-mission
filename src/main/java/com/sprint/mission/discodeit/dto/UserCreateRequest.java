package com.sprint.mission.discodeit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserCreateRequest {

    // 생성할 사용자의 이름
    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(
            min = 2,
            max = 20,
            message = "사용자 이름은 2자 이상 20자 이하여야 합니다."
    )
    private String username;

    // 생성할 사용자의 이메일
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 생성할 사용자의 비밀번호
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(
            min = 4,
            max = 30,
            message = "비밀번호는 4자 이상 30자 이하여야 합니다."
    )
    private String password;

    // 선택적으로 등록할 프로필 이미지 정보
    // 프로필 이미지를 등록하지 않는 경우 null일 수 있음
    @Valid
    private BinaryContentCreateRequest profileImage;

    // 생성자: 사용자 생성에 필요한 값들을 하나의 DTO로 전달받음
    public UserCreateRequest(
            String username,
            String email,
            String password,
            BinaryContentCreateRequest profileImage
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
    }
}