package com.sprint.mission.discodeit.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("관리자 설정의 프로파일별 바인딩 테스트")
class AdminUserProfileTest {
    private static final Map<String, Object> ADMIN_ENV = Map.of(
            "ADMIN_USER_NAME", "configured-admin",
            "ADMIN_USER_PASSWORD", "configured-password",
            "ADMIN_USER_EMAIL", "configured-admin@example.com"
    );

    @Test
    @DisplayName("dev는 환경변수가 없어도 개발용 관리자 설정을 바인딩한다")
    void dev_usesDevelopmentDefaults() {
        runner("dev", Map.of()).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(AdminUserProperties.class)).isEqualTo(new AdminUserProperties(
                    "디스코드잇_관리자", "admin1234", "admin@discodeit.com"));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"dev", "prod"})
    @DisplayName("관리자 환경변수가 프로파일 기본값보다 우선한다")
    void suppliedEnvironment_isUsed(String profile) {
        runner(profile, ADMIN_ENV).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(AdminUserProperties.class)).isEqualTo(new AdminUserProperties(
                    "configured-admin", "configured-password", "configured-admin@example.com"));
        });
    }

    @Test
    @DisplayName("prod는 관리자 환경변수 없이 시작할 수 없다")
    void prod_rejectsMissingConfiguration() {
        runner("prod", Map.of()).run(context -> assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN_USER_NAME", "ADMIN_USER_PASSWORD", "ADMIN_USER_EMAIL"})
    @DisplayName("prod는 관리자 환경변수 중 하나라도 누락되면 바인딩에 실패한다")
    void prod_requiresEveryAdminProperty(String missingKey) {
        Map<String, Object> environment = new HashMap<>(ADMIN_ENV);
        environment.remove(missingKey);
        runner("prod", environment).run(context -> assertThat(context).hasFailed());
    }

    private ApplicationContextRunner runner(String profile, Map<String, Object> environment) {
        return new ApplicationContextRunner()
                .withPropertyValues("spring.profiles.active=" + profile,
                        "spring.config.location=classpath:/application.yml")
                .withInitializer(context -> {
                    var sources = context.getEnvironment().getPropertySources();
                    sources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                    sources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
                    sources.addFirst(new SystemEnvironmentPropertySource("adminTestEnvironment", environment));
                })
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withUserConfiguration(AdminConfiguration.class);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AdminUserProperties.class)
    static class AdminConfiguration {
    }
}
