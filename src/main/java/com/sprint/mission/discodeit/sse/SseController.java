package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * 인증된 사용자의 SSE 연결을 생성하는 HTTP 진입점이다.
 * 수신자 ID는 인증 정보에서 가져오며, 클라이언트의 lastEventId 쿼리 파라미터로 복원 위치를 받는다.
 * 최초 연결은 커서를 생략할 수 있고 응답은 text/event-stream으로 유지된다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@AuthenticationPrincipal DiscodeitUserDetails userDetails,
                              @RequestParam(name = "lastEventId", required = false) UUID lastEventId){

        return sseService.connect(userDetails.getUserDto().id(), lastEventId);

    }
}
