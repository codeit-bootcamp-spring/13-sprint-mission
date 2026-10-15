package com.sprint.mission.discodeit.event.sse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class SseEvent<T> {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final T data;
}
