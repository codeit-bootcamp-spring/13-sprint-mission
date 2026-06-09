package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public interface ReadStatusService {

    ReadStatus create(CreateReadStatusRequest request);

    ReadStatus find(UUID id);

    List<ReadStatus> findAllByUserId(UUID userId);

    void delete(UUID id);

    void update(UUID id, UpdateReadStatusRequest request);

}
