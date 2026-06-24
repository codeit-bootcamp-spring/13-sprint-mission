package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class  GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors=new HashMap<>(); // 오류 결과 담을 Map, key는 필드명, value는 에러 메시지

        // 최대한 변수 선언 없이 method chaining 이용해 호출
        e.getBindingResult().getFieldErrors().forEach(error->{
            errors.put(error.getField(), error.getDefaultMessage());
        });
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "요청 본문에 일부 필드가 유효하지 않습니다.");
        problemDetail.setTitle("입력 검증 실패");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    // 404 - 일치하는 자원이 없을 때
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException e){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                e.getMessage());
        problemDetail.setTitle("없음");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 400 - 잘못된 입력이 요청으로 들어왔을 때
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                e.getMessage());
        problemDetail.setTitle("잘못된 요청");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 400 - JSON 자체가 깨졌거나 enum에 없는 값 등, 요청 본문을 읽지 못할 때
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException e){
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "요청 본문(JSON)을 읽을 수 없습니다. 형식이나 값을 확인하세요.");
        problemDetail.setTitle("요청 본문 오류");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 500 - 그 밖의 예상치 못 한 오류, 원본 메시지는 로그에만 주고 클라이언트에게는 안전한 문구를 준다
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e){
        log.error("예상치 못한 서버 오류", e);
        ProblemDetail problemDetail=ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        problemDetail.setTitle("서버 오류");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

}
