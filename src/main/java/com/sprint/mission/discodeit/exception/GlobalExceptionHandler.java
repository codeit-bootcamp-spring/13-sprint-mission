package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class,ChannelNotFoundException.class,
            MessageNotFoundException.class, BinaryContentNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(DiscodeitException exception) {

        ErrorCode errorCode = exception.getErrorCode();

        log.warn("리소스를 찾을 수 없습니다. code={}, detail={}", exception.getErrorCode(), exception.getDetails());

        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.from(exception));
    }


    @ExceptionHandler(UserAlreadyExistsException.class)
     public ResponseEntity<ErrorResponse> handleConflictException(DiscodeitException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        log.warn("리소스 충돌이 발생했습니다. code={}, detail={}", exception.getErrorCode(), exception.getDetails());

        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.from(exception));
    }

    @ExceptionHandler(PrivateChannelUpdateException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(DiscodeitException exception) {
            ErrorCode errorCode = exception.getErrorCode();

            log.warn("잘못된 요청입니다. code={}, detail={}", exception.getErrorCode(), exception.getDetails());

            return ResponseEntity
                    .status(errorCode.getStatus())
                    .body(ErrorResponse.from(exception));
        }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                ErrorCode.INVALID_REQUEST.name(),
                exception.getMessage(),
                Map.of(),
                exception.getClass().getSimpleName(),
                errorCode.getStatus().value()
        );

        log.warn("잘못된 요청입니다. message={}", exception.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                Map.of(),
                exception.getClass().getSimpleName(),
                errorCode.getStatus().value()
        );

        log.error("처리되지 않은 예외가 발생했습니다.", exception);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

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
                errorCode.getStatus().value()
        );

        log.warn("요청 값 검증에 실패했습니다. errors={}", validationErrors);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.name(),
                "요청 값의 형식이 올바르지 않습니다.",
                Map.of(),
                exception.getClass().getSimpleName(),
                errorCode.getStatus().value()
        );

        log.warn(
                "요청 값 타입 변환에 실패했습니다. name={}, value={}, requiredType={}",
                exception.getName(),
                exception.getValue(),
                exception.getRequiredType()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }




}
