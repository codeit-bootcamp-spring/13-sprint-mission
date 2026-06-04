package com.sprint.mission.discodeit.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 바이너리 데이터 래핑용 엔티티
 */

@Getter
@Setter
@Builder
public class BinaryContent extends BaseEntity{
    private final UUID authorID;
    private final UUID contentID;

    @Override
    public void setUpdatedAt(){}
}
