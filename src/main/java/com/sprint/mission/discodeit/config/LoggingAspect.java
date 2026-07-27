package com.sprint.mission.discodeit.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.sprint.mission.discodeit.controller..*(..))")
    public void controllerMethods(){}

    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();

        log.debug("요청 진입 - {}", signature);
        Object result = joinPoint.proceed();
        log.debug("응답 반환 - {}", signature);

        return result;
    }
}
