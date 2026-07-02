package com.sprint.mission.discodeit.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class BinaryContent extends BaseEntity{
    private final String fileName;
    private final String contentType;
    private final Long size;
    private final byte[] bytes;
}
