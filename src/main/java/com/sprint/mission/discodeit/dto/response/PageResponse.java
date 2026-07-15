package com.sprint.mission.discodeit.dto.response;

import lombok.*;

import java.util.*;

@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int number;
    private final int size;
    private boolean hasNext;
    private final Long totalElements;

    public PageResponse(List<T> content, int number, int size, Long totalElements, boolean hasNext) {
        this.content = content;
        this.number = number;
        this.size = size;
        this.totalElements = totalElements;
        this.hasNext = hasNext;
    }
}
