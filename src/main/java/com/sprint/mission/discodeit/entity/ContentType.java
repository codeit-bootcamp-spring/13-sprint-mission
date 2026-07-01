package com.sprint.mission.discodeit.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ContentType {

    //이미지
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_GIF("image/gif"),

    //문서
    APPLICATION_PDF("application/pdf"),

    //텍스트
    TEXT_PLAIN("text/plain");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
