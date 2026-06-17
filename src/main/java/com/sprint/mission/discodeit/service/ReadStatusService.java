package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public interface ReadStatusService {

    ReadStatusResponse create(CreateReadStatusRequest request);

    ReadStatusResponse find(UUID id);

    List<ReadStatusResponse> findAllByUserId(UUID userId);

    void delete(UUID id);

    void update(UUID id, UpdateReadStatusRequest request);

}
