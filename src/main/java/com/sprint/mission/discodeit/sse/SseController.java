package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Slf4j
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    // 사용자의 경우, 인증 정보를 통해 조회
    @GetMapping(value = "sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> sse(
            @RequestParam(required = false) UUID lastEventId,
            Authentication authentication
    ) {
        if (authentication != null && authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails) {
            UserDto dto = userDetails.getUserDto();

            // lastEvent 는 어떻게?
            return ResponseEntity.ok(sseService.connect(dto.id(),lastEventId));
        } else {
            log.warn("[SSE] 유저 인증 에러 - {}", authentication);
            throw new DiscodeitException(ExceptionCode.INVALID_AUTH_ERROR,"인증 정보가 올바르지 않음");
        }
    }

}
