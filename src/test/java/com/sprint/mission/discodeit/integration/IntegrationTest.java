package com.sprint.mission.discodeit.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("통합 테스트 환경")
class IntegrationTest {

    @Test
    @DisplayName("test 프로파일로 Spring 애플리케이션 컨텍스트를 로딩한다")
    void contextLoads() {
    }
}