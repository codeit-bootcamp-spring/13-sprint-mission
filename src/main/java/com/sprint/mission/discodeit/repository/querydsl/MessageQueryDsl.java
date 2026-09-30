package com.sprint.mission.discodeit.repository.querydsl;

import com.sprint.mission.discodeit.dto.projection.MessageProjection;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MessageQueryDsl {

    Map<UUID, MessageProjection> getMessageProjectionFromIdList(List<UUID> list);
}
