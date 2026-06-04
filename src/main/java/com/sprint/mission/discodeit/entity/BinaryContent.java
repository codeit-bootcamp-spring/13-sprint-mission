package com.sprint.mission.discodeit.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class BinaryContent extends BaseEntity{
    private UUID authorID;
    private UUID contentID;

    @Override
    public void setUpdatedAt(){}
}
