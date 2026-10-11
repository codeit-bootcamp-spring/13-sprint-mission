package com.sprint.mission.discodeit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class AsyncConfigTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void TaskDecorator는_MDC와_인증_정보를_비동기_작업에_전달한다() {
        // given
        AsyncConfig asyncConfig = new AsyncConfig();
        TaskDecorator taskDecorator =
                asyncConfig.contextCopyingTaskDecorator();

        String expectedRequestId = "request-id-123";
        Authentication expectedAuthentication =
                new UsernamePasswordAuthenticationToken(
                        "user@example.com",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        MDC.put("requestId", expectedRequestId);

        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(expectedAuthentication);
        SecurityContextHolder.setContext(securityContext);

        AtomicReference<String> actualRequestId =
                new AtomicReference<>();
        AtomicReference<Authentication> actualAuthentication =
                new AtomicReference<>();

        Runnable decoratedTask = taskDecorator.decorate(() -> {
            actualRequestId.set(MDC.get("requestId"));
            actualAuthentication.set(
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
            );
        });

        // 실제 스레드 풀의 별도 작업 스레드 상태를 재현합니다.
        MDC.clear();
        SecurityContextHolder.clearContext();

        // when
        decoratedTask.run();

        // then
        assertThat(actualRequestId.get())
                .isEqualTo(expectedRequestId);
        assertThat(actualAuthentication.get())
                .isSameAs(expectedAuthentication);

        // 작업 완료 후 실행 스레드의 컨텍스트가 정리되어야 합니다.
        assertThat(MDC.get("requestId")).isNull();
        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }
}