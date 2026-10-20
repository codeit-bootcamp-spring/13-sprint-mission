package com.sprint.mission.discodeit.sse;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class SseMessage {

    static public final UUID BROADCAST_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    /*
    SSE message object
        - data:
        - event:
        - retry:
        - id:
        - :
     */
    private final UUID id = UUID.randomUUID();
    private final UUID receiver;
    private final String name;
    private final Object content;
}
