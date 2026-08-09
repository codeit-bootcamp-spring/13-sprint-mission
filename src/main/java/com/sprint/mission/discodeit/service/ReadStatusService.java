package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;

import java.util.*;

public interface ReadStatusService {

    ReadStatusDto create(CreateReadStatusCommand command);

    ReadStatusDto find(UUID id);

    List<ReadStatusDto> findAllByUserId(UUID userId);

    void delete(UUID id);

    ReadStatusDto update(UUID id, UpdateReadStatusCommand command);

}
