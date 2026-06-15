package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.CreateReadyStatusInput;
import com.sprint.mission.discodeit.dto.input.UpdateReadStatusInput;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    void create(CreateReadyStatusInput crsi);
    ReadStatus find(UUID id);
    List<ReadStatus> findAllByUserID(UUID userID);
    void update(UpdateReadStatusInput ursi);
    void delete(UUID id);
}
