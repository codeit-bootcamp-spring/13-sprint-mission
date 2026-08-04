package com.sprint.mission.discodeit.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("MDC 로깅 통합 테스트")
class MDCLoggingIntegrationTest {

    private static final String REQUEST_ID_HEADER =
            "Discodeit-Request-ID";

    private static final String TEST_API_PATH =
            "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("응답 헤더에 UUID 형식의 요청 ID가 포함된다")
    void responseContainsRequestId() throws Exception {
        MvcResult result = mockMvc.perform(
                        get(TEST_API_PATH)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(REQUEST_ID_HEADER))
                .andReturn();

        String requestId = result.getResponse()
                .getHeader(REQUEST_ID_HEADER);

        assertThat(requestId).isNotBlank();

        assertThatCode(() -> UUID.fromString(requestId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("각 요청마다 서로 다른 요청 ID가 생성된다")
    void eachRequestHasDifferentRequestId() throws Exception {
        MvcResult firstResult = mockMvc.perform(
                        get(TEST_API_PATH)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(REQUEST_ID_HEADER))
                .andReturn();

        MvcResult secondResult = mockMvc.perform(
                        get(TEST_API_PATH)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(REQUEST_ID_HEADER))
                .andReturn();

        String firstRequestId = firstResult.getResponse()
                .getHeader(REQUEST_ID_HEADER);

        String secondRequestId = secondResult.getResponse()
                .getHeader(REQUEST_ID_HEADER);

        assertThat(firstRequestId).isNotBlank();
        assertThat(secondRequestId).isNotBlank();
        assertThat(firstRequestId)
                .isNotEqualTo(secondRequestId);
    }

    @Test
    @DisplayName("존재하지 않는 경로의 응답에도 요청 ID 헤더가 포함된다")
    void errorResponseContainsRequestId() throws Exception {
        mockMvc.perform(
                        get("/not-found")
                )
                .andExpect(status().isNotFound())
                .andExpect(header().exists(REQUEST_ID_HEADER));
    }

    @Test
    @DisplayName("요청 처리가 끝나면 MDC 값이 제거된다")
    void mdcIsClearedAfterRequest() throws Exception {
        mockMvc.perform(
                        get(TEST_API_PATH)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(REQUEST_ID_HEADER));

        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("requestMethod")).isNull();
        assertThat(MDC.get("requestUrl")).isNull();
    }
}