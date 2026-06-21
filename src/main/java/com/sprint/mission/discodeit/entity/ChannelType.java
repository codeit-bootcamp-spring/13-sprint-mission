package com.sprint.mission.discodeit.entity;

import lombok.Getter;

@Getter
public enum ChannelType {
    PRIVATE("비공개", false), // label과 shareable을 추가해 비즈니스 규칙을 이해하기 쉽도록 수정한다
    PUBLIC("공개", true);

    private final String label;
    private final boolean shareable;

    ChannelType(String label, boolean shareable) {
        this.label=label;
        this.shareable=shareable;
    }
}
