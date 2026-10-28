package com.sprint.mission.discodeit.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class MdcSecurityContextTaskDecorator implements TaskDecorator {

  @Override
  @NonNull
  public Runnable decorate(@NonNull Runnable runnable) {
    Map<String, String> contextMap = MDC.getCopyOfContextMap();

    Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();

    return () -> {
      Map<String, String> previousContextMap = MDC.getCopyOfContextMap();
      SecurityContext previousSecurityContext = SecurityContextHolder.getContext();

      try {
        if (contextMap != null) {
          MDC.setContextMap(contextMap);
        } else {
          MDC.clear();
        }

        SecurityContext securityContext =
            SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        runnable.run();

      } finally {
        // 스레드풀은 스레드를 재사용하므로 이번 작업의 컨텍스트를 남기면 안 된다.
        // 호출 스레드가 직접 실행하는 경우에는 clear만 하면 부모 요청의 컨텍스트도 지워진다.
        // 따라서 작업 직전의 컨텍스트를 복원한다.
        MDC.clear();

        if (previousContextMap != null) {
          MDC.setContextMap(previousContextMap);
        }

        SecurityContextHolder.setContext(previousSecurityContext);
      }
    };
  }
}