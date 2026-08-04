package com.sprint.mission.discodeit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {

    // 수정할 사용자의 id
    private UUID id;

    // 기존 코드/프론트 호환용
    @Size(
            min = 2,
            max = 20,
            message = "사용자 이름은 2자 이상 20자 이하여야 합니다."
    )
    @Pattern(
            regexp = ".*\\S.*",
            message = "사용자 이름은 공백으로만 구성할 수 없습니다."
    )
    private String username;

    // 기존 코드/프론트 호환용
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 기존 코드/프론트 호환용
    @Size(
            min = 4,
            max = 30,
            message = "비밀번호는 4자 이상 30자 이하여야 합니다."
    )
    @Pattern(
            regexp = ".*\\S.*",
            message = "비밀번호는 공백으로만 구성할 수 없습니다."
    )
    private String password;

    // API 명세 v1.2 기준 필드
    @Size(
            min = 2,
            max = 20,
            message = "새 사용자 이름은 2자 이상 20자 이하여야 합니다."
    )
    @Pattern(
            regexp = ".*\\S.*",
            message = "새 사용자 이름은 공백으로만 구성할 수 없습니다."
    )
    private String newUsername;

    // API 명세 v1.2 기준 필드
    @Email(message = "올바른 새 이메일 형식이 아닙니다.")
    private String newEmail;

    // API 명세 v1.2 기준 필드
    @Size(
            min = 4,
            max = 30,
            message = "새 비밀번호는 4자 이상 30자 이하여야 합니다."
    )
    @Pattern(
            regexp = ".*\\S.*",
            message = "새 비밀번호는 공백으로만 구성할 수 없습니다."
    )
    private String newPassword;
    // 이미 업로드된 BinaryContent의 id
    private UUID profileId;

    // 프로필 이미지를 새로 업로드해서 교체하는 경우 사용
    @Valid
    private BinaryContentCreateRequest profileImage;

    public UserUpdateRequest(
            UUID id,
            String username,
            String email,
            String password,
            UUID profileId,
            BinaryContentCreateRequest profileImage
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;

        this.newUsername = username;
        this.newEmail = email;
        this.newPassword = password;

        this.profileId = profileId;
        this.profileImage = profileImage;
    }

    /*
     * 서비스 코드에서는 getUsername()을 그대로 사용해도 됨.
     * newUsername이 있으면 newUsername을 우선 사용.
     */
    public String getUsername() {
        if (newUsername != null && !newUsername.isBlank()) {
            return newUsername;
        }

        return username;
    }

    /*
     * 서비스 코드에서는 getEmail()을 그대로 사용해도 됨.
     * newEmail이 있으면 newEmail을 우선 사용.
     */
    public String getEmail() {
        if (newEmail != null && !newEmail.isBlank()) {
            return newEmail;
        }

        return email;
    }

    /*
     * 서비스 코드에서는 getPassword()를 그대로 사용해도 됨.
     * newPassword가 있으면 newPassword를 우선 사용.
     */
    public String getPassword() {
        if (newPassword != null && !newPassword.isBlank()) {
            return newPassword;
        }

        return password;
    }
}