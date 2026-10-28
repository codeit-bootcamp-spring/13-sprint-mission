package com.sprint.mission.discodeit.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discodeit.admin")
public record AdminProperties(

    @NotBlank
    String username,

    @NotBlank @Email
    String email,

    @NotBlank
    String password
) {

}
