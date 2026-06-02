package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.UUID;

public interface ReadStatusRepository {
    public ReadStatus create();
    public ReadStatus getStatusByUserID(UUID id);
    public void delete(UUID id);
}
