package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.exception.storage.StorageException;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ApiErrorResponse> handleDiscodeitException(DiscodeitException e) {
        HttpStatus status = e.getErrorCode().getStatus();

        if (status.is5xxServerError()) {
            logServerError(e);
        } else {
            log.warn(
                    "client error. code={}, message={}",
                    e.getErrorCode(),
                    e.getMessage()
            );
        }

        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(e));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        Map<String, List<String>> fieldsMap = Stream.concat(
                        e.getBindingResult().getGlobalErrors().stream(),
                        e.getBindingResult().getFieldErrors().stream()
                )
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.groupingBy(
                                        this::getFieldNameOrObjectName,
                                        Collectors.mapping(
                                                this::getErrorMessage,
                                                Collectors.collectingAndThen(Collectors.toList(), List::copyOf)
                                        )
                                ),
                                Map::copyOf
                        )
                );

        log.warn("validation errors={}", fieldsMap);

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiErrorResponse.of(
                        errorCode.getStatus().value(),
                        e.getClass().getSimpleName(),
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        fieldsMap
                ));
    }

    private String getFieldNameOrObjectName(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField();
        }

        return error.getObjectName();
    }

    private String getErrorMessage(ObjectError error) {
        return StringUtils.hasText(error.getDefaultMessage()) ? error.getDefaultMessage() : "상세 내용이 없습니다.";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException e) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        log.warn("access denied. message={}", e.getMessage());

        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(
                        status.value(),
                        status.name(),
                        "AUTH_403",
                        "이 작업을 수행할 권한이 없습니다.",
                        Map.of()
                ));
    }

    /**
     * 이미 사용할 수 없는 비동기 응답에는 오류 본문을 다시 쓰지 않는다.
     * 연결 종료 이후 응답 재작성으로 추가 예외가 이어지는 것을 막고 진단 로그만 남긴다.
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException e){
        log.debug("비동기 응답을 더 이상 사용할 수 없습니다. message={}", e.getMessage());
    }

    /**
     * 커밋된 응답의 /error INCLUDE 재처리만 종료한다.
     * 그 외 변환 오류는 기존 공통 예외 처리로 보내 실제 응답 오류를 숨기지 않는다.
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotWritable(HttpMessageNotWritableException e,
                                                                         HttpServletRequest request,
                                                                         HttpServletResponse response) {
        boolean errorInclude = request.getDispatcherType().equals(DispatcherType.INCLUDE) &&
                "/error".equals(request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH));

        if (response.isCommitted() && errorInclude){
            log.debug("이미 전송된 응답의 오류 처리를 종료합니다. message={}", e.getMessage());
            return null;
        }

        return exception(e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> exception(Exception e) {
        log.error("Unhandled exception", e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiErrorResponse.of(
                        errorCode.getStatus().value(),
                        e.getClass().getSimpleName(),
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        Map.of()
                ));
    }

    private void logServerError(DiscodeitException e) {
        if (e instanceof StorageException storageException) {
            log.error(
                    "storage error. code={}, path={}",
                    e.getErrorCode(),
                    getLogPath(storageException),
                    e
            );
            return;
        }

        log.error(
                "application error. code={}, message={}",
                e.getErrorCode(),
                e.getMessage(),
                e
        );
    }

    private Path getLogPath(StorageException storageException) {
        if (storageException.getPath() == null) {
            return null;
        }
        return storageException.getPath().getFileName();
    }

}
