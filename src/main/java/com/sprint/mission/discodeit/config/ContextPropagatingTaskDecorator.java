package com.sprint.mission.discodeit.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ContextPropagatingTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {

    Map<String, String> contextMap = MDC.getCopyOfContextMap();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    return () -> {
      Map<String, String> previousMdc = MDC.getCopyOfContextMap();

      SecurityContext previousContext = SecurityContextHolder.getContext();

      try {
        if (contextMap != null) {
          MDC.setContextMap(contextMap);
        } else {
          MDC.clear();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        runnable.run();

      } finally {
        if (previousMdc != null) {
          MDC.setContextMap(previousMdc);
        } else {
          MDC.clear();
        }

        SecurityContextHolder.setContext(previousContext);
      }
    };
  }
}
