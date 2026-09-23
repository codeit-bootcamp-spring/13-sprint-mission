package com.sprint.mission.discodeit.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("관리자 사용자 설정 검증 테스트")
class AdminUserPropertiesTest {

    @Test
    @DisplayName("관리자 설정이 모두 유효하면 검증을 통과한다")
    void validate_hasNoViolations_whenPropertiesAreValid() {
        AdminUserProperties properties = new AdminUserProperties(
                "admin",
                "adminPassword",
                "admin@example.com"
        );

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            assertThat(validator.validate(properties)).isEmpty();
        }
    }

    @Test
    @DisplayName("관리자 설정이 비어 있으면 필수값 검증에 실패한다")
    void validate_hasViolations_whenPropertiesAreBlank() {
        AdminUserProperties properties = new AdminUserProperties(" ", " ", " ");

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            assertThat(validator.validate(properties))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("username", "password", "email");
        }
    }

    @Test
    @DisplayName("관리자 이메일 형식이 올바르지 않으면 검증에 실패한다")
    void validate_hasViolation_whenEmailIsInvalid() {
        AdminUserProperties properties = new AdminUserProperties(
                "admin",
                "adminPassword",
                "invalid-email"
        );

        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            assertThat(validator.validate(properties))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("email");
        }
    }
}
