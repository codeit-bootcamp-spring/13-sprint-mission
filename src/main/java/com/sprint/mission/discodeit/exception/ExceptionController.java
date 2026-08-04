package com.sprint.mission.discodeit.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    private ProblemDetail from(ExceptionCode code, Map<String, Object> details, Instant timestamp) {
        ProblemDetail pd = ProblemDetail.forStatus(code.status());
        pd.setTitle(code.defaultMessage());
        pd.setProperty("timestamp", timestamp);
        pd.setProperty("code", code.code());
        if (details != null) {
            for (Map.Entry<String, Object> entry : details.entrySet()) {
                pd.setProperty(entry.getKey(),entry.getValue());
            }
        }
        return pd;
    }


    @ExceptionHandler({DiscodeitException.class})
    public ProblemDetail ExceptionHandler(DiscodeitException e) {
        return from(e.code(),e.details(),e.timestamp());
    }

    // 400 — JSON 자체가 깨졌거나 enum 에 없는 값 등, 요청 본문을 읽지 못함
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("body parse error - {}",e.getMessage());
        return from(ExceptionCode.REQUEST_VALUE_ERROR, null, Instant.now());
    }

    // 404 — 매핑된 핸들러가 없는 경로. 프레임워크가 던지는 NoResourceFoundException → C002
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource(NoResourceFoundException e) {
        log.warn("없는 경로 요청: {}", e.getResourcePath());
        return from(ExceptionCode.RESOURCE_NOT_FOUNDED, null, Instant.now());
    }

    // 400 — 경로 변수·쿼리 파라미터의 타입 불일치(예: /activities/abc). 프레임워크 예외 → C001
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("invalid param or query type - {}", e.getMessage());
        return from(ExceptionCode.REQUEST_VALUE_ERROR, null, Instant.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentNotValidException e) {
        log.warn("invalid value - {}", e.getMessage());
        HashMap<String, Object> errors = new HashMap<>();
        errors.put("detail", e.getMessage());
        return from(ExceptionCode.REQUEST_VALUE_ERROR, errors, Instant.now());
    }

    @ExceptionHandler(value = Exception.class)
    public ProblemDetail unhandled(Exception e) {
        log.error("un handled error - {}",e.getMessage(), e);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,"Unhandled server error");
        pd.setProperty("timestamp",Instant.now());
        return pd;
    }
}
