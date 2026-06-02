package com.sprint.mission.discodeit.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class BinaryContent extends BaseEntity{
    UUID authorID;

    @Override
    public void setUpdatedAt(){}
}
