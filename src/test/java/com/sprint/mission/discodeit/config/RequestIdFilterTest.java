package com.sprint.mission.discodeit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private static final String REQUEST_ID_HEADER =
            "X-Request-ID";

    private static final String REQUEST_ID_MDC_KEY =
            "requestId";

    private final RequestIdFilter requestIdFilter =
            new RequestIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void 요청_ID가_없으면_새로운_ID를_생성한다()
            throws Exception {

        // given
        MockHttpServletRequest request =
                new MockHttpServletRequest();
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AtomicReference<String> requestIdInFilterChain =
                new AtomicReference<>();

        // when
        requestIdFilter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) ->
                        requestIdInFilterChain.set(
                                MDC.get(REQUEST_ID_MDC_KEY)
                        )
        );

        // then
        String responseRequestId =
                response.getHeader(REQUEST_ID_HEADER);

        assertThat(responseRequestId).isNotBlank();
        assertThat(requestIdInFilterChain.get())
                .isEqualTo(responseRequestId);
        assertThat(MDC.get(REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void 유효한_요청_ID를_MDC와_응답_헤더에_유지한다()
            throws Exception {

        // given
        String expectedRequestId = "request-id-123";

        MockHttpServletRequest request =
                new MockHttpServletRequest();
        request.addHeader(
                REQUEST_ID_HEADER,
                expectedRequestId
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AtomicReference<String> requestIdInFilterChain =
                new AtomicReference<>();

        // when
        requestIdFilter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) ->
                        requestIdInFilterChain.set(
                                MDC.get(REQUEST_ID_MDC_KEY)
                        )
        );

        // then
        assertThat(requestIdInFilterChain.get())
                .isEqualTo(expectedRequestId);
        assertThat(response.getHeader(REQUEST_ID_HEADER))
                .isEqualTo(expectedRequestId);
        assertThat(MDC.get(REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void 허용되지_않은_문자가_포함된_요청_ID는_새로_생성한다()
            throws Exception {

        // given
        String invalidRequestId = "request_id\r\nforged-log";

        MockHttpServletRequest request =
                new MockHttpServletRequest();
        request.addHeader(
                REQUEST_ID_HEADER,
                invalidRequestId
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AtomicReference<String> requestIdInFilterChain =
                new AtomicReference<>();

        // when
        requestIdFilter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) ->
                        requestIdInFilterChain.set(
                                MDC.get(REQUEST_ID_MDC_KEY)
                        )
        );

        // then
        String generatedRequestId =
                response.getHeader(REQUEST_ID_HEADER);

        assertThat(generatedRequestId)
                .isNotEqualTo(invalidRequestId)
                .matches("^[A-Za-z0-9-]{1,64}$");
        assertThat(requestIdInFilterChain.get())
                .isEqualTo(generatedRequestId);
        assertThat(MDC.get(REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void 길이가_64자를_초과한_요청_ID는_새로_생성한다()
            throws Exception {

        // given
        String invalidRequestId = "a".repeat(65);

        MockHttpServletRequest request =
                new MockHttpServletRequest();
        request.addHeader(
                REQUEST_ID_HEADER,
                invalidRequestId
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AtomicReference<String> requestIdInFilterChain =
                new AtomicReference<>();

        // when
        requestIdFilter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) ->
                        requestIdInFilterChain.set(
                                MDC.get(REQUEST_ID_MDC_KEY)
                        )
        );

        // then
        String generatedRequestId =
                response.getHeader(REQUEST_ID_HEADER);

        assertThat(generatedRequestId)
                .isNotEqualTo(invalidRequestId)
                .matches("^[A-Za-z0-9-]{1,64}$");
        assertThat(requestIdInFilterChain.get())
                .isEqualTo(generatedRequestId);
        assertThat(MDC.get(REQUEST_ID_MDC_KEY)).isNull();
    }
}