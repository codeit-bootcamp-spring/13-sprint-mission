package com.sprint.mission.discodeit.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Size(min = 3, max = 20)
    @NotBlank(message = "userName을 입력해주세요.")
    String username,


    @NotBlank(message = "password를 입력해주세요.")
    String password) {

}
