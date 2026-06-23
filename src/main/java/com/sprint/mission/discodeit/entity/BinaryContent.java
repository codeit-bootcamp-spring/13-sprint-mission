package com.sprint.mission.discodeit.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class BinaryContent extends BaseEntity{
    private final String fileName;
    private final String contentType;
    private final Long size;
    private final byte[] content;

    @Override
    public void setUpdatedAt(){}
}
