package com.sprint.mission.discodeit.sse;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class SseMessage {
    /*
    SSE message object
        - data:
        - event:
        - retry:
        - id:
        - :
     */
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final Object content;
}
