package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.response.*;
import java.util.*;

public interface BinaryContentService {

    BinaryContentDto create(CreateBinaryContentCommand command);

    BinaryContentDto find(UUID id);

    List<BinaryContentDto> findAllByIdIn (List<UUID> ids);

    void delete(UUID id);
}
