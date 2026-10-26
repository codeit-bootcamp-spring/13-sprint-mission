package com.sprint.mission.discodeit.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {

        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        //SecurityContext 객체 공유하는 게 아니라 Authentication만 복사해서 사용하도록
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(SecurityContextHolder.getContext().getAuthentication());

        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }

                SecurityContextHolder.setContext(securityContext);

                runnable.run();

            } finally {
                MDC.clear();
                SecurityContextHolder.clearContext();
            }
        };
    }
}
