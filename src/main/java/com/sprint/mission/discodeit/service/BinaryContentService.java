package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface BinaryContentService {

    BinaryContent create(CreateBinaryContentRequest request);

    BinaryContent find(UUID id);

    List<BinaryContent> findAllByIdIn (List<UUID> ids);

    void delete(UUID id);

}
