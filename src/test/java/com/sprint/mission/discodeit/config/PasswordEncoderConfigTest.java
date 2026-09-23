package com.sprint.mission.discodeit.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordEncoder 설정 테스트")
class PasswordEncoderConfigTest {

    @Test
    @DisplayName("BCryptPasswordEncoder를 생성한다")
    void passwordEncoder_returnsBCryptPasswordEncoder() {
        // given
        PasswordEncoderConfig config = new PasswordEncoderConfig();
        String rawPassword = "testPassword";

        // when
        PasswordEncoder passwordEncoder = config.passwordEncoder();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }
}
