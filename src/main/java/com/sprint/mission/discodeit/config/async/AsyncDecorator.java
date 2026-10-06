package com.sprint.mission.discodeit.config.async;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AsyncDecorator implements TaskDecorator {
    /*
    스레드간 정보를 전달해 주기 위한 클래스 구현.

     */

    @Override
    public Runnable decorate(Runnable runnable) {
        // Mapped Diagnostic Context. @Slf4j 같은 로그 프레임워크의 map 객체?
        // 현재 스레드의 스레드 정보를 map 으로 매핑 해 둔다?
        Map<String, String> callerMdc = MDC.getCopyOfContextMap();

        // 현재(main) 스레드의 인증(요청) 정보를 변수에 저장.
        SecurityContext callerSecurity = SecurityContextHolder.getContext();

        // Runnable 로 자식 스레드 진입점 반환?
        return () -> {
            if (callerMdc != null) {
                MDC.setContextMap(callerMdc); // 로그 정보 반환.
            }
            SecurityContextHolder.setContext(callerSecurity); // 인증 정보 반환.

            try {
                runnable.run();
            } finally {
                // 필수 항목.
                MDC.clear();
                SecurityContextHolder.clearContext();
            }
        };
    }
}
