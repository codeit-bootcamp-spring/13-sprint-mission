package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.exception.binarycontent.*;
import com.sprint.mission.discodeit.exception.channel.*;
import com.sprint.mission.discodeit.exception.message.*;
import com.sprint.mission.discodeit.exception.user.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class,ChannelNotFoundException.class,
            MessageNotFoundException.class, BinaryContentNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(DiscodeitException exception) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        log.warn("리소스를 찾을 수 없습니다. code={}, detail={}", exception.getErrorCode(), exception.getDetails());

        return ResponseEntity.status(status).body(ErrorResponse.from(exception, status.value()));
    }


    @ExceptionHandler(UserAlreadyExistsException.class)
     public ResponseEntity<ErrorResponse> handleConflictException(DiscodeitException exception) {
        HttpStatus status = HttpStatus.CONFLICT;

        log.warn("리소스 충돌이 발생했습니다. code={}, detail={}", exception.getErrorCode(), exception.getDetails());

        return ResponseEntity.status(status).body(ErrorResponse.from(exception, status.value()));
    }

    @ExceptionHandler(PrivateChannelUpdateException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(DiscodeitException exception) {
            HttpStatus status = HttpStatus.BAD_REQUEST;

            log.warn("잘못된 요청입니다. code={}, detail={}", exception.getErrorCode(), exception.getDetails());

            return ResponseEntity
                    .status(status)
                    .body(ErrorResponse.from(exception, status.value()));
        }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                ErrorCode.INVALID_REQUEST.name(),
                exception.getMessage(),
                Map.of(),
                exception.getClass().getSimpleName(),
                status.value()
        );

        log.warn("잘못된 요청입니다. message={}", exception.getMessage());

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                Map.of(),
                exception.getClass().getSimpleName(),
                status.value()
        );

        log.error("처리되지 않은 예외가 발생했습니다.", exception);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, Object> validationErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validationErrors.putIfAbsent(
                        error.getField(),
                        Optional.ofNullable(error.getDefaultMessage())
                                .orElse("잘못된 값입니다.")
                ));

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                ErrorCode.INVALID_REQUEST.name(),
                "요청 값이 올바르지 않습니다.",
                validationErrors,
                exception.getClass().getSimpleName(),
                status.value()
        );

        log.warn("요청 값 검증에 실패했습니다. errors={}", validationErrors);

        return ResponseEntity
                .status(status)
                .body(response);
    }




}
